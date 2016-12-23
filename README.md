# spec-examples

Some examples on using `clojure.spec`!

I've organized some examples I wished I could have seen when I was learning `spec`.

Hope it helps!

## ** \*\*Stars wanted\*\* **

If this repository get **at least 5 stars**, I'll keep pushing **more examples about spec** and I'll **document** them all (with **explanations** and **implementation** details).

To help, me myself have already gave one star!

## Usage

This project is **intended to be used from the REPL**.

It _does not contain a -main nor should be compiled_.

### bindings-utils

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

### dsl-example

After a simple example, here's a more 'intresting' one: a dsl implementation.

All the namespace content is for helping `parse-nosence-lines` `macro`. All it does is parsing _nosence-lang_ code to Clojure. Parsing is what `macro`s are about.


_nosence-lang_ is just a simple language for describing conversations.

Don't look me like that, I couldn't think about anything more useful. 


It should look like the following:

``` text
new Conversation c
new Person Steve
new Person Larry
Steve joins c
Larry joins c
Steve says "Hello!"
Larry says "Hi there!"
Steve says "I hate this place!"
Steve quits
return c

...

participants: Larry
lines: Steve> Hello!
	   Larry> Hi there!
	   Steve> I hate this place!
```


And the Clojure implementation:

``` clojure
user> (require '[spec-examples.dsl-example :as dsl])
nil
user> (dsl/parse-nosence-lines (:new Conversation c)
                               (:new Person Steve)
                               (:new Person Larry)
                               (Steve :joins c)
                               (Larry :joins c)
                               (Steve :says "Hello!")
                               (Larry :says "Hi there!")
                               (Steve :says "I hate this place!")
                               (Steve :quits) 
                               (return c))

=> {:participants #{"Larry"},
    :lines ["Steve> Hello!"
		    "Larry> Hi there!"
		    "Steve> I hate this place!"]}
```

**TODO**: 
  - implementation details
  - spec details

---

Well, guess what? I'm doing it just for fun!


