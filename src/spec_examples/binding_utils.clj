(ns spec-examples.binding-utils
  (:require [clojure.spec :as s]
            [clojure.string :as str]))

(def keystroke-reg
  #"(CTRL-)?\w")

(def keystroke-combo-reg
  (re-pattern (str "("keystroke-reg"\\s)*"keystroke-reg)))

(s/def ::keystroke-combo (s/and string? #(re-matches keystroke-combo-reg %)))
(s/def ::list-of-forms (s/+ list?))

(s/def ::binding (s/cat :keystroke-combo ::keystroke-combo
                        :list-of-forms ::list-of-forms))

(s/fdef map-bindings
        :args (s/+ ::binding))

;; END OF SPEC

(defn get-chars-map
  "Use it and see the magic"
  ([& s-e]
   (let [s-e (partition 2 s-e)]
     (->> (for [[s e] s-e]
            (range (int s) (+ (int e) 1))) 
          flatten
          (reduce (fn [acc v]
                    (assoc acc (-> v char str) v))
                  {})))))

(def ctrl-keys
  (->> (range (int \) (+ (int \) 1)) 
       (reduce (fn [acc v]
                 (assoc acc (->> v (+ 96) char (str "CTRL-")) v))
               {})))


(def avaiable-keys
  (-> (get-chars-map \a \z
                     \A \Z
                     \0 \9)
      (into ctrl-keys)))

(defn convert-keys
  "Converts the string key based on avaiable-keys"
  [keystrokes]
  (for [key (str/split keystrokes #" ")]
    (get avaiable-keys key)))

(defn create-binding
  "Creates a vector representing a keybinding which can be resolved later.
  It takes a string binding and an impure f(unction)."
  [binding f]
  [(convert-keys binding) f])


(defn append-binding
  "Appends a binding to a hash-map of bindings"
  [hash binding f]
  (let [new-binding (create-binding binding f)
        [k v] new-binding]
    (assoc hash k v)))

(defn- append-quoted-binding
  [hash binding f]
  (let [new-binding (create-binding binding f)
        [k v] new-binding]
    (assoc hash `'~k v)))

(defmacro form-func
  "Transforms a form to a function"
  [& forms]
  `(fn [] ~@forms))

(defmacro map-bindings
  [& bindings] 
  (reduce (fn [acc [binding forms]]
            (append-quoted-binding acc binding
                                   `(form-func ~@forms)))
          {} (partition 2 bindings)))



