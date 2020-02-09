(ns collect-reaper-project
  "Collects all of the media files under a new target directory and creates a new Reaper
  project with file references relinked to the new files."
  (:require [clojure.java.io :as io]
            [clojure.zip :as zip]
            [rpp-clj.core :refer [parse-rpp-file output-rpp]])
  (:import [java.nio.file Paths]))

(defn ->Path [s & ss]
  (Paths/get s (into-array String ss)))

(defn unquoted [s]
  (second (re-find #"(?:\")(.*)(?:\")" path)))

(defn quoted [s]
  (str "\"" s "\""))

(defn as-target-path [path target-directory]
  (->> (->Path (unquoted path))
       (.getFileName)
       (.toString)
       (->Path target-directory)
       (.toString)))

(defn file-ref? [loc]
  (= :file (some-> (zip/prev loc) zip/node)))

(defn file-references [dom target-directory]
  (loop [file-refs {}
         loc (zip/vector-zip dom)]
    (if (zip/end? loc)
      file-refs
      (recur
       (if (file-ref? loc)
         (let [path (zip/node loc)]
           (assoc file-refs (unquoted path) (as-target-path path target-directory)))
         file-refs)
       (zip/next loc)))))

(defn copy-files!
  "Takes a map of Reaper media files and desired target paths and makes the copies."
  [file-refs]
  (doseq [[source target] file-refs]
    (io/copy (io/file source) (io/file target))))

(defn replace-file-references [dom file-refs]
  (let [ref-map (->> (map (fn [[s t]] [(quoted s) (quoted t)]) file-refs) (into {}))]
    (loop [loc (zip/vector-zip dom)]
      (if (zip/end? loc)
        (zip/root loc)
        (recur (zip/next
                (if (file-ref? loc)
                  (zip/edit loc ref-map)
                  loc)))))))

(defn -main [& args]
  (when-not (= 2 (count args))
    (println "Usage: clj collect_reaper_project.clj <rpp-file> <target-directory>")
    (System/exit 1))
  (let [[rpp-file target-directory] args
        dom (parse-rpp-file rpp-file)
        file-refs (file-references dom target-directory)
        output-file (str target-directory "/collected.RPP")]

    (copy-files! file-refs)

    (->> (replace-file-references dom file-refs)
         output-rpp
         (spit output-file))))
