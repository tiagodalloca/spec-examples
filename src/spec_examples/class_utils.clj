(ns spec-examples.class-utils
  (:require [clojure.spec :as s]))

;; (make-class
;;  :private [x, y, z]
;;  :immutable [a, b, c]

;;  :get
;;  (x (str x))
;;  (y (.toUpperCase y))
;;  (z (Math/round z))

;;  :set
;;  (x (when value
;;       (set! x))))

(defmacro spec-k [k] `(s/and keyword? #(= ~k %)))

;; SPEC

(def private-k :private)
(def immutable-k :immutable)
(def get-k :get)
(def set-k :set)

(s/def ::col-of-sym (s/coll-of symbol?))
(s/def ::private-keyword (spec-k private-k))
(s/def ::immutable-keyword (spec-k immutable-k))
(s/def ::get-keyword (spec-k get-k))
(s/def ::set-keyword (spec-k set-k))
(s/def ::method-implementation (s/spec (s/cat :method symbol?
                                              :implementation (s/+ any?))))

(s/def ::private-fields (s/cat :private-keyword ::private-keyword
                               :fields ::col-of-sym))
(s/def ::immutable-fields (s/cat :immutable-keyword ::immutable-keyword
                                 :fields ::col-of-sym))
(s/def ::getters (s/cat :get-keyword ::get-keyword
                        :methods (s/+ ::method-implementation)))
(s/def ::setters (s/cat :set-keyword ::set-keyword
                        :methods (s/+ ::method-implementation)))

(s/def ::interface-methods (s/+ (s/spec (s/cat :name symbol?
                                               :args (s/coll-of symbol?)
                                               :body (s/+ any?)))))

(s/def ::interface (s/cat :interface symbol?
                          :methods ::interface-methods))

(s/def ::make-class (s/cat :name symbol?
                           :private-fields (s/? ::private-fields)
                           :immutable-fields (s/? ::immutable-fields)

                           :getters (s/? ::getters)
                           :setters (s/? ::setters)

                           :interfaces (s/* ::interface)))

(clojure.pprint/pprint (s/conform ::make-class '(Hue :private
                                                     [x, y, z]
                                                     :immutable
                                                     [a, b, c]
                                                     :get
                                                     (x (str x))
                                                     (y (.toUpperCase y))
                                                     (z (Math/round z))
                                                     :set
                                                     (x (when value
                                                          (set! x value)))
                                                     INaoSeiOque
                                                     (hue [] 1))))

;; END OF SPEC

(defn volatile-mutable-metadata
  [vect]
  (reduce #(conj %1 %2 `(with-meta (quote ~%2)
                          {:volatile-mutable true})) [] vect))

(defn parse-conformed-interface
  [{:keys [interface methods]}]
  `(~interface ~@(reduce (fn [acc {:keys [name args body]}]
                           (conj acc `(~name ~args ~@body))) '() methods)))

(defn parse-make-class
  [x]
  (let [conformed (s/conform ::make-class x)]
    (when (not= conformed :clojure.spec/invalid)
      (let [{name :name
             {p-fields :fields} :private-fields
             {i-fields :fields} :immutable-fields
             {g-methods :methods} :getters
             {s-methods :methods} :setters
             interfaces :interfaces} conformed
            fields (apply conj i-fields p-fields)
            parsed-interfaces
            (apply concat
                   (reduce #(conj %1 (parse-conformed-interface %2)) '()
                           interfaces))]
        `(let ~(volatile-mutable-metadata p-fields)
           (deftype ~name ~fields ~@parsed-interfaces))))))

;; (clojure.pprint/pprint (parse-make-class '(Hue :private
;;                                                [x, y, z]
;;                                                :immutable
;;                                                [a, b, c]
;;                                                :get
;;                                                (x (str x))
;;                                                (y (.toUpperCase y))
;;                                                (z (Math/round z))
;;                                                :set
;;                                                (x (when value
;;                                                     (set! x value)))
;;                                                INaoSeiOque
;;                                                (hue [this] 1))))

