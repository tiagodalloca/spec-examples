(ns spec-examples.dsl-example
  (:require [clojure.spec :as s]
            [clojure.string :as str])
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
  (print-ctor x (fn [o w] (print-dup ((str \" x \")) w))
              w))

(defn conversation []
  (Conversation. #{} []))

(defn person [name]
  (Person. name nil))

(defmacro handle-get
  [obj-sym field-sym]
  (let [getter (str ".get" (-> field-sym str str/capitalize))]
    (read-string (str "(" getter " " obj-sym ")"))))

(defn handle-return [x] x)

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

(defn print-conversation
  [conversation])

(def constructors-map
  {'Conversation (fn [_] (conversation))
   'Person #(person (str %))})

(defn construct-thing [constructor-s args]
  `(apply ~(constructors-map constructor-s) '~args))

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

;; END OF SPEC

(defmulti parse-nosence
  "Core function for parsing the nosencelang"
  (fn [& args] (map class (take 1 args))))

(defmethod parse-nosence
  [Symbol]
  sym-k-args
  [s1 k & args]
  `(~(k infix-map) ~s1 ~@args))
(defmethod parse-nosence
  [Keyword]
  k-args 
  [k & args]
  `(~(k prefix-map) ~@args))

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

