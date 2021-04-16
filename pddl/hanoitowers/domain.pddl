;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; 3 discs hanoi tower
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;Header and description

(define (domain hanoi)

;remove requirements that are not needed
(:requirements :strips)


(:predicates ;todo: define predicates here
	(clear ?x)	; disk x is clear
	(on ?x ?y)	; disk x in y
	(smaller ?x ?y)	; x is smaller than y
)

(:action move
	:parameters (?disc ?from ?to)
	:precondition (and
		(on ?disc ?from)
		(smaller ?disc ?to)
		(smaller ?disc ?from)
		(clear ?disc)
		(clear ?to)
	)
	:effect (and 
		(clear ?from)
		(not (clear ?to))
		(not (on ?disc ?from))
		(on ?disc ?to)
	)
)



;define actions here

)