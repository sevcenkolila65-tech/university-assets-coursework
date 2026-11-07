package ua.edu.nupp.kpp.server;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseworkTest {
 @Test void scoreUsesSpecifiedWeights(){assertEquals(75.0,RiskCalculator.calculate(75,75,75),0.001);}
 @Test void scoreAtZeroIsZero(){assertEquals(0.0,RiskCalculator.calculate(0,0,0),0.001);}
 @Test void scoreAtHundredIsHundred(){assertEquals(100.0,RiskCalculator.calculate(100,100,100),0.001);}
 @Test void scoreIsRoundedToTwoDecimals(){assertEquals(56.5,RiskCalculator.calculate(50,60,62),0.001);}
 @Test void negativeFactorRejected(){assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(-1,50,50));}
 @Test void factorAboveHundredRejected(){assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(50,101,50));}
 @Test void nanFactorRejected(){assertThrows(IllegalArgumentException.class,()->RiskCalculator.calculate(Double.NaN,50,50));}
 @Test void scarcityWhenAllAvailableIsZero(){assertEquals(0.0,RiskCalculator.scarcity(10,10),0.001);}
 @Test void scarcityWhenNoneAvailableIsHundred(){assertEquals(100.0,RiskCalculator.scarcity(10,0),0.001);}
 @Test void scarcityForHalfStockIsFifty(){assertEquals(50.0,RiskCalculator.scarcity(10,5),0.001);}
 @Test void zeroTotalIsFullDeficit(){assertEquals(100.0,RiskCalculator.scarcity(0,0),0.001);}
 @Test void invalidAvailabilityRejected(){assertThrows(IllegalArgumentException.class,()->RiskCalculator.scarcity(5,6));}
 @Test void createdCanBeApproved(){assertTrue(Lifecycle.allowed(RequestState.CREATED,RequestState.APPROVED));}
 @Test void approvedCanBeIssued(){assertTrue(Lifecycle.allowed(RequestState.APPROVED,RequestState.ISSUED));}
 @Test void issuedCanBeReturned(){assertTrue(Lifecycle.allowed(RequestState.ISSUED,RequestState.RETURNED));}
 @Test void cannotSkipLifecycleState(){assertFalse(Lifecycle.allowed(RequestState.CREATED,RequestState.ISSUED));}
 @Test void cannotReturnBeforeIssue(){assertFalse(Lifecycle.allowed(RequestState.APPROVED,RequestState.RETURNED));}
 @Test void transitionReturnsTarget(){assertEquals(RequestState.APPROVED,Lifecycle.transition(RequestState.CREATED,RequestState.APPROVED));}
 @Test void invalidTransitionThrows(){assertThrows(IllegalArgumentException.class,()->Lifecycle.transition(RequestState.CREATED,RequestState.RETURNED));}
}
