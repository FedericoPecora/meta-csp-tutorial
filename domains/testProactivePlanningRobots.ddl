##################
# Reserved words #
#################################################################
#                                                               #
#   Head                                                        #
#   Resource                                                    #
#   Sensor                                                      #
#   ContextVariable                                             #
#   SimpleOperator                                              #
#   SimpleDomain                                                #
#   Constraint                                                  #
#   RequiredState												#
#   AchievedState												#
#   RequriedResoruce											#
#   All AllenIntervalConstraint types                           #
#   '[' and ']' should be used only for constraint bounds       #
#   '(' and ')' are used for parsing                            #
#                                                               #
#################################################################

(Domain TestProactivePlanning)

(Sensor Location)
(Sensor Stove)

(ContextVariable Human)

(Actuator Robot)

(SimpleOperator
 (Head Human::Cooking())
 (RequiredState req1 Location::Kitchen())
 (RequiredState req2 Stove::On())
 (Constraint Overlaps(Head,req1))
 (Constraint Contains(Head,req2))
)

(SimpleOperator
 (Head Human::Eating())
 (RequiredState req1 Location::DiningRoom())
 (RequiredState req2 Human::Cooking())
 (RequiredState req3 Robot::SayWarning())
 (Constraint Finishes(Head,req1))		#Eating Finishes DiningRoom
 (Constraint After(Head,req2))			#Eating After Cooking
)

(SimpleOperator
 (Head Robot::SayWarning())
 (RequiredState req1 Robot::MoveTo())
 (Constraint MetBy(Head,req1))
 (Constraint Duration[2000,INF](Head))
)

(SimpleOperator
 (Head Robot::MoveTo())
 (Constraint Duration[2000,INF](Head))
)