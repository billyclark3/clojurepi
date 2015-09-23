# Pi Estimate with Clojure

This is a web service that yields estimates of the irrational number Pi. There are two primary endpoints served via Compojure: /chart/:function and /list/:function, with various parameters. These endpoints yield, respectively, scatter-plot charts (PNGs dynamically generated with Incanter) or lists (JSON) of estimates for different sample sizes. The :function parameter is the function name, with the two functions presently supported being the Leibniz formula and the Euler formula, both of which represent Pi as infinite series. The default sample sizes used in these estimates range from 1 to 1000 iterations of either series. A different range of sample sizes can be requested with the HTTP params start and size. So a request to /chart/leibniz?start=100&size=500 will return a PNG response that charts estimates of the Leibniz representation with 100 iterations, 101, 102, etc, up to 600 iterations. Larger sample sizes will of course take more time to compute.

The series are represented in Clojure with the aid of lazy sequences. Incanter was helpful for its sqrt function as well as the generation of PNG charts.