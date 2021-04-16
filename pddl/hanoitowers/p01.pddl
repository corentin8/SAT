(define (problem p01) (:domain hanoi)
(:objects d1 d2 d3 p1 p2 p3)

(:init
	(clear p2)
	(clear p3)
	(clear d1)

	(on d2 d3)
	(on d1 d2)
	(on d3 p1)

	(smaller d1 d2)
	(smaller d1 d3)
	(smaller d2 d3)

	; discs are smaller than Ps d1 d2 d3 p3
	(smaller d1 p1)
	(smaller d1 p2)
	(smaller d1 p3)

	(smaller d2 p1)
	(smaller d2 p2)
	(smaller d2 p3)

	(smaller d3 p1)
	(smaller d3 p2)
	(smaller d3 p3)
)

(:goal (and
	(on d1 d2)
	(on d2 d3)
	(on d3 p3)
))

;un-comment the following line if metric is needed
;(:metric minimize (???))
)
