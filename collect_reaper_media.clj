(ns jogo3000.collect-reaper-media
  (:require [clojure.string :as str]))

(def rpp-file (slurp "/home/jogo3000/Documents/REAPER Media/arrangement3/arrangement3.RPP"))

(def state
  {:stack []
   :state :idle
   :group []
   :symbol []
   :node []})

(defn store-symbol [node symbol]
  (if(empty? symbol) node
     (conj node (apply str symbol))))

(defn consume [{:keys [group stack state symbol node] :as s} c]
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
             :group []
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
             :node (if-not (empty? symbol) (conj node (apply str symbol)) node)
             :symbol [])

      (= \" c)
      (assoc s :state :quoted :symbol [])

      :else
      (assoc s :symbol (conj symbol c)))))

(defn parse-rpp [rpp-string]
  (reduce consume state rpp-file))

(defn parse-rpp-file [path-to-rpp]
  (parse-rpp (slurp path-to-rpp)))
