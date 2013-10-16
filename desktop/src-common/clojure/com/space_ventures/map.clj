(ns com.space-ventures.map
  (:import [com.badlogic.gdx.maps.tiled TiledMap TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers OrthogonalTiledMapRenderer]))

(defn load-map [map-name]
  (let [tilemap (.load (TmxMapLoader.) map-name)]
    (hash-map :tilemap tilemap
              :renderer (OrthogonalTiledMapRenderer. tilemap))))

(defn render-map [mapmap camera]
  (.setView (mapmap :renderer) camera)
  (.render (mapmap :renderer)))