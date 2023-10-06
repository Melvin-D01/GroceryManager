import android.content.Context
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import project.stn991614740.grocerymanagerapp.CatGridAdapter
import project.stn991614740.grocerymanagerapp.CatGridItem

class CatGridAdapterTest {

    private val context = mockk<Context>(relaxed = true)
    private val onClick = mockk<(CatGridItem) -> Unit>(relaxed = true)
    private val dataSet = listOf(
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.fruits, "Fruit", "fruits"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.vegetable, "Vegetable", "vegetable"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.meat, "Meat", "meat"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.seafood, "Seafood", "seafood"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.dairy, "Dairy", "dairy"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.grains, "Grains", "grains"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.canfood, "Canned Goods", "canfood"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.snack, "Snacks", "snack"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.bev, "Beverages", "bev"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.condiments, "Condiments", "condiments"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.bakery, "Baked Goods", "bakery"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.frozenfood, "Frozen Foods", "frozenfood"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.bento, "Prepped Meals", "bento"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.babyfood, "Baby Food", "babyfood"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.petfood, "Pet Food", "petfood"),
        CatGridItem(project.stn991614740.grocerymanagerapp.R.drawable.menu, "Other Food", "menu")
    )
    private lateinit var adapter: CatGridAdapter

    @Before
    fun setUp() {
        adapter = CatGridAdapter(context, dataSet, onClick)
    }

    @Test
    fun `getItemCount should return correct item count`() {
        // Arrange
        // setup is already done in the @Before setup method

        // Act
        val itemCount = adapter.itemCount

        // Assert
        assertEquals(dataSet.size, itemCount)
    }

    @Test
    fun `onBindViewHolder should bind data correctly`() {
        // Arrange
        val viewHolder = mockk<CatGridAdapter.CatGridViewHolder>(relaxed = true)
        val index = 0
        val item = dataSet[index]

        // Act
        adapter.onBindViewHolder(viewHolder, index)

        // Assert
        verify { viewHolder.bind(item) }
    }


}
