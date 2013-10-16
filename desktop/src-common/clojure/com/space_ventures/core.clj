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



(ns com.space-ventures.core
  (:require [com.space-ventures.map :as tilemap]
            [com.space-ventures.character :as character])
  (:import [com.badlogic.gdx Game Gdx Graphics Screen Input$Keys]
           [com.badlogic.gdx.graphics GL10 Color OrthographicCamera]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.utils TimeUtils]))


; TODO:
; 1) make a new file for the main character. Create functions to move him
; and make sure the functions change the sprite accordingly.
; 2) Extend the map to somehow contain obstacle information (in another layer?)
; 3) Create another file doing path finding for the main character using the
; current position, a target and the obstacle map

(def asset-path "../android/assets/")

(def ortocamera nil)
(def batch nil)
(def world nil)

(def main-screen
  (proxy [Screen] []
    (show [] )
    (render [delta]
      (.glClearColor (Gdx/gl) 0 0 0.2 1)
      (.glClear (Gdx/gl) GL10/GL_COLOR_BUFFER_BIT)
        
      
      ;(if (.isKeyPressed Gdx/input Input$Keys/LEFT)
        ;(set! (. bucket x)
        ;      (- (. bucket x)(* 200 (.getDeltaTime Gdx/graphics)))))
      ;(if (.isKeyPressed Gdx/input Input$Keys/RIGHT)
        ;(set! (. bucket x)
        ;      (+ (. bucket x) (* 200 (.getDeltaTime Gdx/graphics)))))  
      
      ;(if (.isTouched Gdx/input)
        ;(let [touch-pos (Vector3.)]
          ; convert the touch coordinates (from input) into the
          ; coordinate system of the camera
          ;(.set touch-pos (.getX Gdx/input) (.getY Gdx/input) 0)
          ;(.unproject ortocamera touch-pos)
          ;(set! (. bucket x) (- (. touch-pos x) (/ 64 2)))))
      
      ;(if (< (. bucket x) 0)
      ;  (set! (. bucket x) 0))
      ;(if (> (. bucket x) (- 640 64))
      ;  (set! (. bucket x) (- 640 64)))
      
      (.update ortocamera)
      (tilemap/render-map (world :tilemap) ortocamera)
      
      (let [direction (cond
                        (.isKeyPressed Gdx/input Input$Keys/UP) 0
                        (.isKeyPressed Gdx/input Input$Keys/RIGHT) 270
                        (.isKeyPressed Gdx/input Input$Keys/DOWN) 180
                        (.isKeyPressed Gdx/input Input$Keys/LEFT) 90
                        :else -1)
            speed (cond
                    (.isKeyPressed Gdx/input Input$Keys/UP) 200
                    (.isKeyPressed Gdx/input Input$Keys/RIGHT) 200
                    (.isKeyPressed Gdx/input Input$Keys/DOWN) 200
                    (.isKeyPressed Gdx/input Input$Keys/LEFT) 200
                    :else 0)]
        (def world (assoc world :character
                          (character/move
                            (world :character) direction speed (.getDeltaTime Gdx/graphics)))))
      
      (.setProjectionMatrix batch (. ortocamera combined))
      (.begin batch)
      (character/render (world :character) batch)
      (.end batch))
    
    (dispose[]
      ;(.dispose droplet-img)
      ;(.dispose bucket-img)
      (.dispose batch))
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))


(gen-class
  :name com.space-ventures.core.Game
  :extends com.badlogic.gdx.Game)

(defn -resize [this w h]
  (println "resized to" w "x" h))

(defn -create [this]
  (.setScreen this main-screen)
  (def ortocamera (OrthographicCamera.))
  (.setToOrtho ortocamera false 640 640)
  (def batch (SpriteBatch.))
  (def world (hash-map
               :tilemap (tilemap/load-map "/home/jivi/graphics/space_ventures/testing/simple_walled_map.tmx")
               :character (character/create 320 320 64 64 (str asset-path "character")))))

