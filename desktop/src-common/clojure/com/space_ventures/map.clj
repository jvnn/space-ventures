; Copyright (C) 2013  Jussi Nieminen

; This program is free software; you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 2 of the License, or
; (at your option) any later version.

; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.

; You should have received a copy of the GNU General Public License along
; with this program; if not, write to the Free Software Foundation, Inc.,
; 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.



(ns com.space-ventures.map
  (:require [clojure.algo.generic.math-functions :as math])
  (:import [com.badlogic.gdx.maps.tiled TiledMap TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.maps.objects RectangleMapObject]
           [com.badlogic.gdx.math Rectangle]))


; get the obstacle layer from the map and extract all rectangle objects
(defn- extract-obstacles [tilemap scale]
  (let [objects (.getObjects (.get (.getLayers tilemap) "obstacles"))
        rectangles (seq (.getByType objects RectangleMapObject))]
    (for [rect-obj rectangles]
      (let [rectangle (.getRectangle rect-obj)
            x-scaled (* (. rectangle x) scale)
            y-scaled (* (. rectangle y) scale)
            width-scaled (* (. rectangle width) scale)
            height-scaled (* (. rectangle height) scale)]
        (.setX rectangle x-scaled)
        (.setY rectangle y-scaled)
        (.setWidth rectangle width-scaled)
        (.setHeight rectangle height-scaled)
        rectangle))))


(defn- extract-floorgraph [tilemap]
  (let [nonpassable (.get (.getLayers tilemap) "obstacle_tiles")]
    (doseq [x (range 0 (.getWidth nonpassable))
          y (range 0 (.getHeight nonpassable))]
      (nil? (.getCell nonpassable x y)))))




(defn- get-distance
  "get the heuristics estimate for A*"
  [fromx fromy tox toy]
  (math/sqrt (+ (math/pow (- fromx tox) 2) (math/pow (- fromy toy) 2))))

(defn- points-equal?
  [x1 y1 x2 y2]
  (and (= x1 x2) (= y1 y2)))

(defn- construct-path
  "use the path map to create the final path"
  [pathmap startx starty goalx goaly]
  (loop [path ()
         x goalx
         y goaly]
    (if (points-equal? x y startx starty)
      path
      (let [newpoint (pathmap [x y])
            newx (first newpoint)
            newy (second newpoint)]
        (recur (conj path [newx newy]) newx newy)))))

(defn- rm-obstacle-neighbours
  [neighbours mapmap character]
  (let [obstacles (mapmap :obstacles)
        width (character :width)
        height (character :height)
        area (Rectangle. 0 0 width height)]
    (filter #(not (nil? %))
            (for [nb neighbours]
              (let [nbx (first nb)
                    nby (last nb)]
                (.setX area nbx)
                (.setY area nby)
                (if (some #(.overlaps % area) obstacles)
                  nil
                  [nbx nby]))))))

(defn- update-open-vector
  [neighbours checked open gscore fscore path cx cy destx desty]
  (loop [nbs neighbours
         opn open
         gs gscore
         fs fscore
         p path]
    (if-let [nb (first nbs)]
      (let [x (first nb)
            y (second nb)
            ; neighbours are always one step away
            tmp-gs (+ (gs [cx cy]) 1)
            tmp-fs (+ tmp-gs (get-distance x y destx desty))
            is-checked (some #(points-equal? (first %) (second %) x y) checked)
            in-open (some #(points-equal? (first %) (second %) x y) opn)]
        (if (and is-checked (>= tmp-fs (fs [x y])))
          ; neighbour is checked and no need to update f-score values
          (recur (rest nbs) opn gs fs p)
          ; otherwise add to the open set or update scores
          (if (or (not in-open) (< tmp-fs (fs [x y])))
            (recur
              (rest nbs)
              (if in-open opn (conj opn nb))
              (assoc gs [x y] tmp-gs)
              (assoc fs [x y] tmp-fs)
              (assoc p [x y] [cx cy]))
            ; else we just go forward again
            (recur (rest nbs) opn gs fs p))))
      ; else for if-let: we don't have any more neighbours in the list.
      ; Sort the open vector so that we get the one with smallest fscore next
      {:open (sort-by #(fs %) opn) :gscore gs :fscore fs :path p})))
            

(defn get-path
  "A* path between two locations avoiding obstacles"
  [mapmap character sourcex sourcey destx desty]
  
  ; open contains item with [x-coord, y-coord, g-val, f-val]
  (loop [checked #{}
         open [[sourcex sourcey]]
         gscore {[sourcex sourcey] 0}
         fscore {[sourcex sourcey] (get-distance sourcex sourcey destx desty)}
         path {}]
    ; if we haven't found a path and nothing in open, nil will be returned
    (when-let [current (first open)]
      (let [cx (first current)
            cy (second current)]
        (if (points-equal? cx cy destx desty)
          (construct-path path sourcex sourcey cx cy)
          ; else: not in target yet, add fitting neighbours to open
          (let [neighbours (for [dx (range -1 2)
                                 dy (range -1 2) :when
                                 (and (not (= dx dy))
                                      (not (zero? (+ dx dy))))]
                             [(+ cx dx) (+ cy dy)])
                valids (rm-obstacle-neighbours neighbours mapmap character)
                ; now we need to check whether we want to add the remaining neighbours
                ; to the open vector or update the g/f values or discard completely
                newdata (update-open-vector valids
                                            checked
                                            (rest open)
                                            gscore
                                            fscore
                                            path
                                            cx cy destx desty)]
            (recur (conj checked [cx cy]) (newdata :open) (newdata :gscore) (newdata :fscore) (newdata :path))))))))



(defn create [map-name scale]
  (let [tilemap (.load (TmxMapLoader.) map-name)]
    (println (extract-floorgraph tilemap))
    (hash-map :tilemap tilemap
              :renderer (OrthogonalTiledMapRenderer. tilemap (float scale))
              :obstacles (extract-obstacles tilemap scale)
              :scale scale)))

(defn get-obstacles [mapmap]
  (mapmap :obstacles))

(defn render [mapmap camera]
  (.setView (mapmap :renderer) camera)
  (.render (mapmap :renderer)))


(defn dispose [mapmap]
  (.dispose (mapmap :tilemap))
  (.dispose (mapmap :renderer)))
