(ns class-utils
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
                                              :implementation list?)))

(s/def ::private-fields (s/cat :private-keyword ::private-keyword
                               :fields ::col-of-sym))
(s/def ::immutable-fields (s/cat :immutable-keyword ::immutable-keyword
                                 :fields ::col-of-sym))
(s/def ::getters (s/cat :get-keyword ::get-keyword
                        :methods (s/+ ::method-implementation)))
(s/def ::setters (s/cat :set-keyword ::set-keyword
                        :methods (s/+ ::method-implementation)))
(s/def ::interface (s/cat :interface symbol?
                          :methods (s/+ list?)))

(s/def ::make-class (s/cat :private-fields (s/? ::private-fields)
                           :immutable-fields (s/? ::immutable-fields)

                           :getters (s/? ::getters)
                           :setters (s/? ::setters)

                           :interfaces (s/* ::interface)))

;; (pprint (s/conform ::make-class '(:private
;;                                   [x, y, z]
;;                                   :immutable
;;                                   [a, b, c]
;;                                   :get
;;                                   (x (str x))
;;                                   (y (.toUpperCase y))
;;                                   (z (Math/round z))
;;                                   :set
;;                                   (x (when value
;;                                        (set! x))))))

;; END OF SPEC
