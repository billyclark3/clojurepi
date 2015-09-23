(ns newcomp.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
			[clojure.string :as string]
			[cheshire.core :as json]
			[incanter.core :refer :all]
			[incanter.stats :refer :all]
			[incanter.charts :refer :all]
			[incanter.datasets :refer :all]
			[ring.middleware.json :refer [wrap-json-response]]
			[ring.util.response :refer [response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
			
			(:import (java.io ByteArrayOutputStream
			                    ByteArrayInputStream)))
			

	 (defn seq-of-nodes
	 	[func n init]
	 	(take n
	 		(map func (iterate inc init)) 
	 		)
	 	)
	 (defn series-gen
	 	[func n init]
	 	(reduce + (seq-of-nodes func n init))
	 	)



	 ; This is one node in the Euler infinite series representing Pi
	 (defn leibniz-node [n] 	
	 	(let [
	 			oddnum (+ (* 2.0 n) 1.0)
	 			signfactor (- 1 (* 2 (mod n 2)))
	 		]
	 		(/ (* 4.0 signfactor) oddnum)
	 	)
	 )

	 ; This is one node in the Leibniz infinite series representing Pi
	 (defn euler-node [n]  
	 	(/ 6.0 (* n n)	
	 	))


	 (defn series-euler
	 	[n]
	 	(reduce + (seq-of-nodes euler-node n 1))
	 	)

	 (defn series-leibniz
	 	[n]
	 	(reduce + (seq-of-nodes leibniz-node n 0))
	 	)



	(defn my-euler-dataset [n init]
		(take n ( map #(sqrt (series-euler %)) (iterate inc init)))
	)


	(defn my-leibniz-dataset [n init]
		(take n ( map series-leibniz (iterate inc init)))
	)	


	(defn gen-png
	  [dataset size-str init-str]
	    (let [ init (Integer/parseInt init-str)
			size (if (nil? size-str)
	                 100
	                 (Integer/parseInt size-str))
			chart (scatter-plot (take size (iterate inc init)) (dataset size init) :x-label "N" :y-label "Estimate" )
			out-stream (ByteArrayOutputStream.)
			    in-stream (do
			                (save chart out-stream)
			                (ByteArrayInputStream.
			                  (.toByteArray out-stream)))
							  ]
		  	    		    {:body in-stream
		  						:status 200
		  	    		            :headers {"Content-Type" "image/png"}}

									))
				 

	(defn json-response [data & [status]]
	  {:status  (or status 200)
	   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
	   :body    (json/generate-string data)})



(defroutes app-routes
  
  (GET "/list/:func" req
	(let [size-str (get (:params req) :size "1000")
	  start-str (get (:params req) :start "1")
	  func-kw (keyword (get (:params req) :func "leibniz")) ; leibniz is default
	  my-func (func-kw {:leibniz my-leibniz-dataset :euler my-euler-dataset})
       ]
	   	(json-response (my-func (Integer/parseInt size-str) (Integer/parseInt start-str)))
			  )
  )
  
  (GET "/chart/:func" req
	(let [size-str (get (:params req) :size "1000")
	  start-str (get (:params req) :start "1")
	  func-kw (keyword (get (:params req) :func "leibniz")) ; leibniz is default
	  my-func (func-kw {:leibniz my-leibniz-dataset :euler my-euler-dataset})
       ]
		     (gen-png my-func  size-str  start-str)
			  )
  )  
  
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
