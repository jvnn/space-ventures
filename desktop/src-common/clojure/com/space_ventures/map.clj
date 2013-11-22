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
  (:import [com.badlogic.gdx.maps.tiled TiledMap TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.maps.objects RectangleMapObject]))


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
        (set! (. rectangle x) x-scaled)
        (set! (. rectangle y) y-scaled)
        (set! (. rectangle width) width-scaled)
        (set! (. rectangle height) height-scaled)
        rectangle))))


(defn- extract-floorgraph [tilemap]
  (let [nonpassable (.get (.getLayers tilemap) "obstacle_tiles")]
    (doseq [x (range 0 (.getWidth nonpassable))
          y (range 0 (.getHeight nonpassable))]
      (nil? (.getCell nonpassable x y)))))


(defn create [map-name scale]
  (let [tilemap (.load (TmxMapLoader.) map-name)]
    (println (extract-floorgraph tilemap))
    (hash-map :tilemap tilemap
              :renderer (OrthogonalTiledMapRenderer. tilemap (float scale))
              :obstacles (extract-obstacles tilemap scale)
              :scale scale)))

(defn obstacles [mapmap]
  (mapmap :obstacles))

(defn render [mapmap camera]
  (.setView (mapmap :renderer) camera)
  (.render (mapmap :renderer)))


(defn dispose [mapmap]
  (.dispose (mapmap :tilemap))
  (.dispose (mapmap :renderer)))
