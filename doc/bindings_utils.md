# bindings-utils

This namespace contains functions for mapping keystrokes and functions.

**Example**

``` clojure
user> (require '[spec-examples.binding-utils.clj])

=> nil

user> (bu/convert-keys "CTRL-c s") ;; represents a 'CTRL-s' keystroke followed by a 's' 

=> (3 115) ;; sequence of key codes

user> (bu/map-bindings "CTRL-c s" ((println "You pressed CTRL-c s!")
                                   (println "Yes, you!"))
                       "CTRL-s" ((println "You pressed CTRL-s!")
                                 (println "Are you trying to save a file?")))

=> {(3 115) #function[user/eval18254/fn--18255],
    (19) #function[user/eval18254/fn--18257]}
```

**`spec`**

``` clojure
(def keystroke-reg
  #"(CTRL-)?\w")

(def keystroke-combo-reg
  (re-pattern (str "("keystroke-reg"\\s)*"keystroke-reg)))

(s/def ::keystroke-combo (s/and string? #(re-matches keystroke-combo-reg %)))
(s/def ::list-of-forms (s/+ list?))

(s/def ::binding (s/cat :keystroke-combo ::keystroke-combo
                        :list-of-forms ::list-of-forms))

(s/fdef map-bindings
        :args (s/+ ::binding)) ;; spec'ing map-bindings macro (view example above)
```
