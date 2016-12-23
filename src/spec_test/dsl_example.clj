(ns spec-test.dsl-example
  (:require [clojure.spec :as s])
  (:import [clojure.lang Keyword Symbol]))

(definterface IConversation
  (getParticipants [])
  (setParticipants [n])
  (updateParticipants [f])
  (getLines [])
  (setLines [n])
  (updateLines [f]))

(definterface IPerson
  (getName [])
  (getConversation [])
  (setConversation [c]))

(deftype Conversation [^:volatile-mutable participants
                       ^:volatile-mutable lines]
  Object
  (toString [this]
    (str {:participants participants
          :lines lines }))
  IConversation
  (getParticipants [this]
    participants)
  (setParticipants [this n]
    (set! participants n))
  (updateParticipants [this f]
    (set! participants (f participants)))
  (getLines [this]
    lines)
  (setLines [this n]
    (set! lines n))
  (updateLines [this f]
    (set! lines (f lines))))

(deftype Person [name
                 ^:volatile-mutable conv]
  Object
  (toString [this]
    (str name))
  IPerson
  (getName [this]
    name)
  (getConversation [this]
    conv)
  (setConversation [this c]
    (set! conv c)))

(defmethod print-method Conversation [x writer]
  (.write writer (.toString x)))

(defmethod print-method Person [x writer]
  (.write writer (str \" x \")))

(defmethod print-dup Conversation [x w]
  (print-ctor x (fn [o w] (print-dup (str x) w))
              w))

(defmethod print-dup Person [x w]
  (print-ctor x (fn [o w] (print-dup (str x) w))
              w))

(defn conversation []
  (Conversation. #{} []))

(defn person [name]
  (Person. name nil))

(defn add-participant
  [who conversation]
  (.updateParticipants conversation #(conj % who))
  (.setConversation who conversation))

(defn say-something
  [who what]
  (let [c (.getConversation who)]
    (if (some #{who} (.getParticipants c))
      (.updateLines c #(conj % (str who "> "what))))))

(defn quit-conversation
  [who]
  (let [c (.getConversation who)]
    (if (some #{who} (.getParticipants c))
      (do (.updateParticipants c #(disj % who))
          (.setConversation who nil)))))

(def constructors-map
  {'Conversation (fn [sym] (conversation))
   'Person #(person (str %))})

(defn construct-thing [constructor-s args]
  `(apply ~(constructors-map constructor-s) '~args))

(def keyword-map
  {:enters `add-participant
   :says `say-something
   :quits `quit-conversation})

(def new-k :new)

;; SPEC

(defn has?
  [v coll]
  (some #{v} coll))

(s/def ::nosence-keyword (s/and keyword? #(has? % (keys keyword-map))))
(s/def ::nosence-new-keyword (s/and keyword? #(= % new-k)))
(s/def ::nosence-constructor
  (s/and symbol? #(has? % (keys constructors-map))))
(s/def ::nosence-args (s/* any?))
(s/def ::nosence-symbol (s/spec symbol?))

(s/def ::nosence-constructor-line (s/cat :new-k ::nosence-new-keyword
                                         :constructor ::nosence-constructor
                                         :obj-symbol ::nosence-symbol
                                         :args ::nosence-args))

(s/def ::nosence-regular-line (s/cat :subject ::nosence-symbol
                                     :keyword ::nosence-keyword
                                     :args ::nosence-args))

(s/def ::nosence-line (s/or :constructor-line ::nosence-constructor-line
                            :regular-line ::nosence-regular-line))

(s/fdef parse-nosence-lines
        :args (s/cat :body-lines (s/+ ::nosence-line) :last-line (s/? any?)))

;; END OF SPEC

(defmulti parse-nosence
  "Core function for parsing the nosencelang"
  (fn [x & ys] (map class (into [x] ys))))

(defmethod parse-nosence
  [Symbol Keyword Symbol]
  sym-k-sym
  [s1 k s2]
  `(~(k keyword-map) ~s1 ~s2))
(defmethod parse-nosence [Symbol Keyword String]
  sym-k-s
  [s1 k s]
  `(~(k keyword-map) ~s1 ~s))
(defmethod parse-nosence [Symbol Keyword]
  [s1 k]
  `(~(k keyword-map) ~s1))
(defmethod parse-nosence :default [x] x)

(defn bindings-from-new [lists]
  (->> (for [[k constructor sym & args] lists
             :when (= k new-k)] 
         `(~sym ~(construct-thing constructor (conj args sym))))
       (apply concat)
       (into [])))

(defn parse-nosence-lines-helper [lines]
  (let [bindings (bindings-from-new lines)
        lines (remove #(= new-k (first %)) lines)]
    `(let ~bindings
       ~@(for [l lines] 
           (apply parse-nosence l)))))

(defmacro parse-nosence-lines
  [& lines]
  (parse-nosence-lines-helper lines))

;; (parse-nosence-lines
;;  (:new Conversation c)
;;  (:new Person Steve)
;;  (Steve :in c)
;;  (Steve :says "Hello")
;;  (Steve :says "I hate this place!")
;;  (Steve :quits)
;;  (c))

