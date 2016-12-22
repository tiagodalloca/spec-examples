(ns spec-test.core
  (:require [clojure.spec :as s]))

(def key-regex
  #"((CTRL|META|\w)-)?\w")

(def keycombo-regex
  (re-pattern (str "("key-regex"\\s)*"key-regex)))

(s/def ::str-keycombo (s/and string? #(re-matches keycombo-regex %)))
(s/def ::list-of-forms (s/+ list?))

(s/def ::binding (s/cat :str-keycombo   ::str-keycombo
                        :list-of-forms  ::list-of-forms))

(s/def ::map-of-bindings (s/coll-of ::binding))

