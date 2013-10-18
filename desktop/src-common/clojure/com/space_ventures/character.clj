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


(ns com.space-ventures.character
  (:require [clojure.algo.generic.math-functions :as math])
  (:import [com.badlogic.gdx Gdx]
           [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.math Rectangle]))

(def CHANGE-WALK-MODE-DIST 1)
(def PI 3.14159)
(def DEG2RAD (/ PI 180))

(defn create [x y width height texture-base scale]
  (hash-map :texture (hash-map :still (Texture. (.internal (Gdx/files) (str texture-base "_still.png")))
                               :walk1 (Texture. (.internal (Gdx/files) (str texture-base "_walk1.png")))
                               :walk2 (Texture. (.internal (Gdx/files) (str texture-base "_walk2.png"))))
            :area (Rectangle. (- x (/ width 2)) (- y (/ height 2)) width height)
            :walk-state 1
            :walk-state-dist 0
            :x x
            :y y
            :width width
            :height height
            :speed 0
            :direction 0
            :scale scale))

; walk-state means which texture (1 or 2) to use.
; walk-state-dist keeps track of how far the character has walked to and when
; a limit is exceeded, the other texture will be used.
(defn move [character direction speed delta-time obstacles]
  (let [newdir (if (>= direction 0)
                 direction
                 (character :direction))
        ; the -/+ and sin/cos below were just decided with trial&error...
        distance (* speed delta-time)
        dx (* (math/sin (* direction DEG2RAD)) (- 0 distance))
        dy (* (math/cos (* direction DEG2RAD)) distance)
        newx (+ (character :x) dx)
        newy (+ (character :y) dy)
        new-walk-dist (if (>= speed 0)
                        (+ distance (character :walk-state-dist))
                        0)
        new-walk-state (if (> new-walk-dist CHANGE-WALK-MODE-DIST)
                         (+ (mod (character :walk-state) 2) 1)
                         (character :walk-state))
        area (character :area)
        scale (character :scale)]
    
    ; update the character area to test for collisions
    (set! (. area x) (- newx (/ (character :width) 2)))
    (set! (. area y) (- newy (/ (character :height) 2)))
    (if (some (fn [obstacle] (.overlaps obstacle area)) obstacles)
      ; there is an obstacle that overlaps with the new area. Don't move!
      (assoc character :speed 0 :direction newdir :walk-state-dist 0)
      (assoc character :speed speed :direction newdir :x newx :y newy
           :walk-state new-walk-state
           :walk-state-dist (mod new-walk-dist CHANGE-WALK-MODE-DIST)))))


(defn render [character ^SpriteBatch batch]
  ; move the character's position based on speed and directions
  (let [speed (character :speed)
        texture (if (= speed 0)
                  ((character :texture) :still)
                  (if (= (character :walk-state) 1)
                    ((character :texture) :walk1)
                    ((character :texture) :walk2)))
        texture-width (.getWidth texture)
        texture-height (.getHeight texture)
        x-middle (character :x)
        y-middle (character :y)
        x (- x-middle (/ texture-width 2))
        y (- y-middle (/ texture-height 2))
        direction (character :direction)
        scale (character :scale)]
    ; arguments for the monster draw:
    ; x-pos, y-pos, origin-x, origin-y, width, height, scale-x, scale-y,
    ; rotation, texture-region-start-x, texture-region-start-y,
    ; region-width, region-height, flip-x, flip-y.....
    (.draw batch texture x y (/ texture-width 2) (/ texture-height 2)
      texture-width texture-height scale scale direction 0 0
      texture-width texture-height false false)))


(defn dispose [character]
  (doseq [texture (vals (character :texture))]
    (.dispose texture)))

