package project.stn991614740.grocerymanagerapp

// This import is for the Assert class
import org.junit.Assert.*
import org.junit.Test

class AddFragmentTest {

    // Create an instance of AddFragment to test its methods
    private val fragment = AddFragment()

    @Test
    fun testIsValidFoodItemName_validName_returnsTrue() {
        // Arrange
        val validName = "Apple"

        // Act
        val result = fragment.isValidFoodItemName(validName)

        // Assert
        assertTrue(result)
    }

    @Test
    fun testIsValidFoodItemName_blankName_returnsFalse() {
        // Arrange
        val blankName = " "

        // Act
        val result = fragment.isValidFoodItemName(blankName)

        // Assert
        assertFalse(result)
    }

    @Test
    fun testIsValidFoodItemName_longName_returnsFalse() {
        // Arrange
        val longName = "a".repeat(51)  // 51 'a's

        // Act
        val result = fragment.isValidFoodItemName(longName)

        // Assert
        assertFalse(result)
    }

    // Example of Testing Cleanup
    @Test
    fun testOnDestroyView_setsBindingToNull() {
        // Arrange
        val fragment = AddFragment()
        // Assume that the fragment is currently displaying its view...

        // Act
        fragment.onDestroyView()

        // Assert
        assertNull(fragment._binding)  // Verify that _binding is null
    }
}