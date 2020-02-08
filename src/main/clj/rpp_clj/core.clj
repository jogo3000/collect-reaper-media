(ns rpp-clj.core
  (:require [clojure.string :as str]))

(def ^{:private true} state
  {:stack []
   :state :idle
   :group []
   :symbol []
   :node []})

(defn- store-symbol [node symbol]
  (if (empty? symbol) node
     (conj node (apply str symbol))))

(defn- keywordize-attr [node]
  (let [[attr & params] node
        attr (-> (str/lower-case attr) (str/replace #"_" "-") keyword)]
    (vec (cons attr params))))

(defn- store-node [group node]
  (if (empty? node) group
      (conj group (keywordize-attr node))))

(defn- consume [{:keys [group stack state symbol node] :as s} c]
  (if (= state :quoted)
    (if-not (= c \")
      (assoc s :symbol (conj symbol c))
      (assoc s
             :state :idle
             :symbol []
             :node (conj node (str \" (apply str symbol) \"))))
    (cond
      (= \< c)
      (assoc s
             :stack (conj stack group)
             :group [:<]
             :node [])

      (= \> c)
      (assoc s
             :state :idle
             :stack (pop stack)
             :group (conj (last stack) group)
             :node [])

      (or (= \return c)
          (= \newline c))
      (let [node (if-not (empty? symbol) (conj node (apply str symbol)) node)]
        (assoc s :state :idle
               :symbol []
               :node []
               :group (store-node group node)))

      (= \space c)
      (assoc s
             :node (store-symbol node symbol)
             :symbol [])

      (= \" c)
      (assoc s :state :quoted :symbol [])

      :else
      (assoc s :symbol (conj symbol c)))))

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
  (->> (repeat (* 2 level) " ") (apply str)))

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
            (list (str hanging-ingress \>))))
)

(defn output-rpp
  "Takes a DOM representation and outputs a string representation in the RPP file format"
  [dom]
  (->> (output-group dom 0)
       (str/join \newline)))
