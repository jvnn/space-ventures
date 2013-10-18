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
           [com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer]))

(defn create [map-name]
  (let [tilemap (.load (TmxMapLoader.) map-name)]
    (hash-map :tilemap tilemap
              :renderer (OrthogonalTiledMapRenderer. tilemap))))

(defn render [mapmap camera]
  (.setView (mapmap :renderer) camera)
  (.render (mapmap :renderer)))


(defn dispose [mapmap]
  (.dispose (mapmap :tilemap))
  (.dispose (mapmap :renderer)))
