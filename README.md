# rpp-clj
Parser for Reaper project files

# Rationale
Reaper is my favourite DAW. Sometimes the user interface trips me and
I end up making edits that I regret. A lot of them. In these cases
sometimes I've written tools for automatically correct the mistakes I
made. Sometimes it's easy, sometimes it's not. The RPP file format
used by Reaper is not too easy to parse and process, as it is "almost
XML" but not quite.

Therefore, I decided to write a parser which I can use to help create my
Reaper project cleaning tools.

# Usage
Require rpp-clj:

```clojure
(require '[rpp-clj.core :refer [parse-rpp-file output-rpp]))
```

Parse a DOM of the project file:

```clojure
(def dom (parse-rpp-file "/REAPER_MEDIA/project.RPP"))

;; Creates a DOM with this structure.
;; [:<
;;  [:reaper-project "0.1" "\"5.979/linux64\"" "1581079158"]
;;  [:ripple "0"]
;;  [:groupoverride "0" "0" "0"]
;;  [:autoxfade "1"]
;;  [:envattach "1"]
;;  .
;;  .
;;  .
;;   [:< [:record-cfg]]
;;   [:< [:applyfx-cfg]]


```

The structure is a nested vector representation of the project
file. Nesting structures start with **:<** keyword. Attributes and their
parameters are represented as vectors. The attribute names are
keywordized to a Clojure-like representation. Parameters are
represented as strings. Note that when Reaper has a quoted
parameter, this is wrapped to quotes in this representation as well
(see **:reaper-project** attribute parameters for example).

You can make any manipulations you want to this structure and then
render a new project with **output-rpp**:

```clojure
(output-rpp dom)
```

It creates a String representation which you can save to a RPP project
file.

See [/examples](./examples) for more details.

# TODO
Is it a good idea to further parse the arguments? Currently everything
is considered a String argument. It might be easier to manipulate the
DOM if those types would be changed to native types. The problem with
this approach is that all of the String typed arguments aren't
systemically quoted. That makes it somewhat harder to render the DOM
as a Reaper project file when your edits have been done. I didn't
test if Reaper would accept quoted strings everywhere yet.
