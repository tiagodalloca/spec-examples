(ns spec-examples.class-maker
  (:require [clojure.spec :as s]
            [clojure.string :as str]))

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

;; END OF SPEC

(defn make-it-mutable
  [x]
  (read-string (str "^{:volatile-mutable true} " x)))

(defn parse-conformed-interface
  [{:keys [interface methods]}]
  `(~interface ~@(map (fn [{:keys [name args body]}] `(~name ~args ~@body))
                      methods)))

(defn interfacefy-methods
  [class-name g-methods s-methods]
  `(definterface ~(symbol (str "I" class-name))
     ~@(map (fn [{:keys [method implementation]}]
              `(~(symbol (str "get" (str/capitalize (str method))))
                []))
         g-methods)
     ~@(map (fn [{:keys [method implementation]}]
              `(~(symbol (str "set" (str/capitalize (str method))))
                [~'value]))
         s-methods)))

(defn gen-interfacefied-implementation
  [class-name g-methods s-methods]
  `(~(symbol (str "I" class-name))
    ~@(map (fn [{:keys [method implementation]}]
             `(~(symbol (str "get" (str/capitalize (str method))))
               [~'this] ~@implementation))
           g-methods)
    ~@(map (fn [{:keys [method implementation]}]
             `(~(symbol (str "set" (str/capitalize (str method))))
               [~'this ~'value] ~@implementation))
           s-methods)))

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
            fields (into [] (concat (map make-it-mutable p-fields) i-fields))
            parsed-interfaces
            (apply concat
                   (reduce #(conj %1 (parse-conformed-interface %2)) '()
                           interfaces))
            class-interface
            (interfacefy-methods name g-methods s-methods)
            interface-implementation
            (gen-interfacefied-implementation name g-methods s-methods)]
        `(~class-interface
          (deftype ~name ~fields
            ~@parsed-interfaces
            ~@interface-implementation))))))

(defmacro make-class
  [& x]
  `(do ~@(parse-make-class x)))

