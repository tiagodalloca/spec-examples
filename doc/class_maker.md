# class-maker (please, do not ever use it in production)

Clojure definitely is not the right language for using classes all around, specially if they're meant to be **mutable**. It usually comes with boilerplate.

But Clojure is a lisp.

This mean macros.

So we can extend it's syntax...

![I have a bad feeling...](ihaveabadfeeling_luke.gif)

## Demonstration

```clojure
user> (require '[spec-examples.class-maker :as ck])
=> nil

user> (ck/make-class ClassExample
                     :private [x y z]
                     :immutable [a b c]
                     :get
                     (x x)
                     (y (println "getY called")
                        (str y))
                     :set
                     (x (when value
                          (set! x value))) ;; value is setX's arg
                     (y (when value
                          (set! y (inc value))))
                     Object
                     (toString [this]
                               (str "x: " x  \newline
                                    "y: " y  \newline
                                    "z: " z  \newline
                                    "a: " a  \newline
                                    "b: " b  \newline
                                    "c: " c  \newline)))
=> user.ClassExample

user> (def obj (ClassExample. "X" 42 "Z" 1 2 3))
=> #'user/obj

user> (.getY obj)
getY called
=> "42"

user> (.setY obj 0)
=> 1

user> (.getY obj)
getY called
=> "1"

user> (println (str obj))
x: X
y: 1
z: Z
a: 1
b: 2
c: 3
=> nil
```

