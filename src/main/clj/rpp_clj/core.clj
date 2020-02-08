(ns rpp-clj.core
  (:require [clojure.string :as str]))

(def ^private state
  {:stack []
   :state :idle
   :group []
   :symbol []
   :node []})

(defn- store-symbol [node symbol]
  (if(empty? symbol) node
     (conj node (apply str symbol))))

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
               :group (if-not (empty? node) (conj group node) group)))

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
  (->> (reduce consume state rpp-file)
       :group
       first))

(defn parse-rpp-file
  "Takes a path to an RPP file and returns a DOM representation."
  [path-to-rpp]
  (parse-rpp (slurp path-to-rpp)))


(defn- nesting [level]
  (->> (repeat (* 2 level) " ") (apply str)))

(defn- output-group [node level]
  (let [hanging-ingress (nesting level)
        ingress (nesting (inc level))
        [_ child & children] node
        first-row (str hanging-ingress "<" (str/join \space child))]
    (concat (cons first-row
                  (map #(if (= :< (first %))
                          (str/join \newline (output-group % (inc level)))
                          (str ingress (str/join \space %))) children))
            (list (str hanging-ingress \>))))
)

(defn output-rpp
  "Takes a DOM representation and outputs a string representation in the RPP file format"
  [dom]
  (->> (output-group dom 0)
       (str/join \newline)))
