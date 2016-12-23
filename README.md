# spec-test

Some examples on using clojure.spec!


## Usage

This project is **intended to be used from the REPL**.

It _does not contains a -main nor should be compiled_.

### bindings-utils

This namespace contains functions for mapping keyboard keys and functions.

**Example**

``` clojure
user> (bu/convert-keys "CTRL-c s") ;; represents a 'CTRL-s' stroke followed by a 's' stroke 

(3 115)
;; sequence of key codes

user> (bu/map-bindings "CTRL-c s" ((println "You pressed CTRL-c s!")
                                   (println "Yes, you!"))
                       "CTRL-s" ((println "You pressed CTRL-s!")
                                 (println "Are you trying to save a file?")))

{(3 115) #function[user/eval18254/fn--18255],
 (19) #function[user/eval18254/fn--18257]}
```

**`spec`**

``` clojure
(def key-regex
  #"(CTRL-)?\w")

(def keycombo-regex
  (re-pattern (str "("key-regex"\\s)*"key-regex)))

(s/def ::str-keycombo (s/and string? #(re-matches keycombo-regex %)))
(s/def ::list-of-forms (s/+ list?))

(s/def ::binding (s/cat :str-keycombo   ::str-keycombo
                        :list-of-forms  ::list-of-forms))

(s/fdef map-bindings
        :args (s/+ ::binding)) ;; spec'ing map-bindings macro (view example above)
```

---

Well, guess what? I'm doing it just for fun!


