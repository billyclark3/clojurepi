(ns simple_web_app
  (:gen-class)
  (:use [compojure]
        [compojure.http response]
		 [ring.util.response :refer [resource-response response]]
        [incanter core stats charts datasets])
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
		

	 (defn series-gen-euler
	 	[n]
	 	(reduce + (seq-of-nodes euler-node n 1))
	 	)

	 (defn series-gen-leibniz
	 	[n]
	 	(reduce + (seq-of-nodes leibniz-node n 0))
	 	)



	(defn my-euler-dataset [n]
		(take n ( map #(sqrt (series-gen-euler %)) (iterate inc 1)))
	)

	
	(defn my-leibniz-dataset [n]
		(take n ( map series-gen-leibniz (iterate inc 1)))
	)	

  (defn gen-png
    [dataset request size-str]
      (let [size (if (nil? size-str)
                   100
                   (Integer/parseInt size-str))
  		chart (scatter-plot (take size (iterate inc 1)) (dataset size)  )
  		out-stream (ByteArrayOutputStream.)
  		    in-stream (do
  		                (save chart out-stream)
  		                (ByteArrayInputStream.
  		                  (.toByteArray out-stream)))
  		    header {:status 200
  		            :headers {"Content-Type" "image/png"}}]


  		(update-response request
  		                 header
  		                 in-stream)))
						 
					
	(defroutes webservice\
	 	(GET "/euler"
	    	(gen-png my-euler-dataset request
	 	   	   (params :size)
			 )
		)
	 	(GET "/leibniz"
	    	(gen-png my-leibniz-dataset request
	 	   	   (params :size)
			 )
		)		
	)

 
 (defn -main [& args]
   (run-server {:port 8080}
     "/*" (servlet webservice)))