(ns clirj.components)

(defn button
  [text f & [opts]]
  [:button (merge {:on-click f} opts) text])

(defn input
  [f & [opts]]
  [:input (merge {:on-change f} opts)])
