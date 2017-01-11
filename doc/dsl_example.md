## dsl-example (aka _nosence-lang_)

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
                               (return c))

=> {:participants #{"Larry"},
    :lines ["Steve> Hello!"
		    "Larry> Hi there!"
		    "Steve> I hate this place!"]}
```
