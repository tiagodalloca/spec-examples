# dsl-example (aka _nosence-lang_)

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


And the Clojure version:

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
                               (:return c))

=> {:participants #{"Larry"},
    :lines ["Steve> Hello!"
		    "Larry> Hi there!"
		    "Steve> I hate this place!"]}
```

## Explanation

### `spec` stuff

This dsl is composed by "lines" (lists, actually). **There's three kinds of lines:**

- Constructor
- Infix
- Prefix

We'll take a look in each one of them.

#### Constructor

```clojure
(s/def ::new-keyword (s/and keyword? #(= % new-k)))
(s/def ::constructor (s/and symbol? (set (keys constructors-map))))
(s/def ::args (s/* any?))

(s/def ::constructor-line (s/spec (s/cat :new-k ::new-keyword
                                         :constructor ::constructor
                                         :obj-symbol ::symbol
                                         :args ::args)))
```

In English:

A constructor line has **a 'new' keyword followed by a constructor name (which has to be a symbol), a object symbol (the name of the object that will be created) and 0 or more arguments (that may vary according to the constructor)**



#### Infix

```clojure
(s/def ::infix-keyword (s/and keyword? (set (keys infix-map))))
(s/def ::args (s/* any?))

(s/def ::infix-line (s/spec (s/cat :subject symbol?
                                   :keyword ::infix-keyword
                                   :args ::args)))
```

A infix line has **a symbol, a infix keyword and 0 or more arguments (in this order)**

#### Prefix

```clojure
(s/def ::prefix-keyword (s/and keyword? (set (keys infix-map))))
(s/def ::args (s/* any?))

(s/def ::prefix-line (s/spec (s/cat :keyword ::prefix-keyword
                                    :args ::args)))
```

A infix line has **a prefix keyword and 0 or more arguments**

#### Wrapping the specs

```clojure
(s/def ::line (s/alt :infix-line ::infix-line
                     :prefix-line ::prefix-line))

(s/def ::lines-seq (s/cat :constructor-lines (s/* ::constructor-line)
                          :body-lines (s/+ ::line)))

(s/fdef parse-nosence-lines :args ::lines-seq)
```

Nosence-lang code should be **preceded by constructor lines and then 1 or more regular lines (which might be either a infix or a prefix line)**

The macro `parse-nosence-lines` has it's arguments `spec`ed by `::lines-seq`.

The whole thing:
```clojure
(def constructors-map
  {'Conversation (fn [_] (conversation))
   'Person #(person (str %))}))

(def infix-map
  {:joins `add-participant
   :says `say-something
   :quits `quit-conversation})

(def prefix-map
  {:print `print
   :print-line `println
   :get `handle-get
   :return `handle-return})

(def new-k :new)

;; SPEC

(defn has?
  [v coll]
  (some #{v} coll))

(s/def ::infix-keyword (s/and keyword? (set (keys infix-map))))
(s/def ::prefix-keyword (s/and keyword? (set (keys prefix-map))))
(s/def ::new-keyword (s/and keyword? #(= % new-k)))
(s/def ::constructor (s/and symbol? (set (keys constructors-map))))
(s/def ::args (s/* any?))

(s/def ::constructor-line (s/spec (s/cat :new-k ::new-keyword
                                         :constructor ::constructor
                                         :obj-symbol symbol?
                                         :args ::args)))

(s/def ::infix-line (s/spec (s/cat :subject symbol?
                                   :keyword ::infix-keyword
                                   :args ::args)))

(s/def ::prefix-line (s/spec (s/cat :keyword ::prefix-keyword
                                    :args ::args)))

(s/def ::line (s/alt :infix-line ::infix-line
                     :prefix-line ::prefix-line))

(s/def ::lines-seq (s/cat :constructor-lines (s/* ::constructor-line)
                          :body-lines (s/+ ::line)))

(s/fdef parse-nosence-lines :args ::lines-seq)
```

