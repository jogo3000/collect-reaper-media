(ns rpp-clj.core
  (:require [clojure.string :as str]))

(def ^{:private true} state
  {:stack []
   :mode :default
   :group []
   :symbol []
   :node []})

(defn- store-symbol [node symbol]
  (if (empty? symbol) node
     (conj node (str/join symbol))))

(defn- keywordize-attr [node]
  (let [[attr & params] node
        attr (-> (str/lower-case attr) (str/replace #"_" "-") keyword)]
    (vec (cons attr params))))

(defn- store-node [group node]
  (if (empty? node) group
      (conj group (keywordize-attr node))))

(defn- consume-quoted [{:keys [symbol node] :as state} c]
  (if-not (= c \")
    (assoc state :symbol (conj symbol c))
    (assoc state
           :mode :default
           :symbol []
           :node (conj node (str \" (str/join symbol) \")))))

(defn- consume-default [{:keys [group stack symbol node] :as state} c]
  (cond
    (= \< c)
    (assoc state
           :mode :default
           :stack (conj stack group)
           :group [:<]
           :node [])

    (= \> c)
    (assoc state
           :mode :default
           :stack (pop stack)
           :group (conj (last stack) group)
           :node [])

    (or (= \return c)
        (= \newline c))
    (let [node (store-symbol node symbol)]
      (assoc state
             :mode :default
             :symbol []
             :node []
             :group (store-node group node)))

    (= \space c)
    (assoc state
           :node (store-symbol node symbol)
           :symbol [])

    (= \" c)
    (assoc state
           :mode :quoted
           :symbol [])

    :else
    (assoc state :symbol (conj symbol c))))

(defn- consume [{:keys [mode] :as state} c]
  (if (= mode :quoted)
    (consume-quoted state c)
    (consume-default state c)))

(defn parse-rpp
    "Take an RPP file in string format and outputs a DOM representation. Every <
  starts a new vector with :< as the first element. All other parameters are stored
  as vectors with the keyword in the beginning and each parameter as their own element"
  [rpp-string]
  (->> (reduce consume state rpp-string)
       :group
       first))

(defn parse-rpp-file
  "Takes a path to an RPP file and returns a DOM representation."
  [path-to-rpp]
  (parse-rpp (slurp path-to-rpp)))

(defn- kw->attr-name [kw]
  (-> (name kw) (str/replace #"-" "_") (str/upper-case)))

(defn- nesting [level]
  (->> (repeat (* 2 level) " ") (str/join)))

(defn- output-header [header level]
  (let [ingress (nesting level)
        [kw & args] header
        header-name (kw->attr-name kw)]
    (str ingress "<" (str/join \space (cons header-name args)))))

(defn- output-attribute [attr level]
  (let [ingress (nesting level)
        [kw & args] attr
        attr-name (kw->attr-name kw)]
    (str ingress (str/join \space (cons attr-name args)))))

(defn- output-group [node level]
  (let [hanging-ingress (nesting level)
        [_ header & children] node
        first-row (output-header header level)]
    (concat (cons first-row
                  (map #(if (= :< (first %))
                          (str/join \newline (output-group % (inc level)))
                          (output-attribute % (inc level))) children))
            (list (str hanging-ingress \>)))))

(defn output-rpp
  "Takes a DOM representation and outputs a string representation in the RPP file format"
  [dom]
  (->> (output-group dom 0)
       (str/join \newline)))
