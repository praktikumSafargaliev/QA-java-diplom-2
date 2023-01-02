import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    private Ingredient ingredient;
    private Response response;
    private UserAuthInfo userAuthInfo;
    private User user;
    private final String baseURI = "https://stellarburgers.nomoreparties.site";
    private final String orderCreateEndpoint = "/api/orders/";
    private final String userRegistrationEndpoint = "/api/auth/register/";
    private final String userDataEndpoint = "/api/auth/user/";
    private final String ingredient_1 = "61c0c5a71d1f82001bdaaa6d"; // "Флюоресцентная булка R2-D3"
    private final String ingredient_2 = "61c0c5a71d1f82001bdaaa6f"; // "Мясо бессмертных моллюсков Protostomia"
    private final String expectedOrderName = "Флюоресцентный бессмертный бургер";
    private final String testEmail = "autotestruslan@ya.ru";
    private final String testPassword = "autotest";
    private final String testName = "Ruslan";
    private final ArrayList<String> ingredients = new ArrayList<>();

    @Before
    public void setUp() {
        RestAssured.baseURI = baseURI;
        ingredients.add(ingredient_1);
        ingredients.add(ingredient_2);
        ingredient = new Ingredient(ingredients);
    }

    @Test
    @DisplayName("Создаём заказ без токена авторизации")
    public void createOrderWithoutTokenTest() {
        createOrderWithoutToken()
                .then().statusCode(200)
                .and().assertThat().body("success", equalTo(true), "name", equalTo(expectedOrderName), "order.number", notNullValue());
    }

    @Test
    @DisplayName("Создаём заказ с токеном авторизации")
    public void createOrderWithTokenTest() {
        createUser();
        createOrderWithToken()
                .then().statusCode(200)
                .and().assertThat().body("success", equalTo(true), "name", equalTo(expectedOrderName), "order.number", notNullValue());
        deleteUser();
    }

    @Test
    @DisplayName("Создаём заказ с пустым списком ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        ingredients.clear();
        createOrderWithoutToken()
                .then().statusCode(400)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создаём заказ с невалидным списком интегредиентов")
    public void createOrderWithInvalidIngredientsTest() {
        ingredients.set(0, "");
        createOrderWithoutToken().then().statusCode(500);
    }



    @Step("Удаляем пользователя")
    public void deleteUser() {
        if (getAccessToken() != null) {
            given().header("Authorization", getAccessToken()).delete(userDataEndpoint);
        }
    }

    @Step("Отправляем запрос на создание заказа без авторизации")
    public Response createOrderWithoutToken() {
        response = given().headers("Content-type", "application/json").body(ingredient).post(orderCreateEndpoint);
        return response;
    }


    @Step("Отправляем запрос на создание заказа с токеном авторизации")
    public Response createOrderWithToken() {
        response = given().headers("Authorization", getAccessToken(),  "Content-type", "application/json").body(ingredient).post(orderCreateEndpoint);
        return response;
    }

    @Step("Создаём пользователя и кладём тело ответа в класс UserAuthInfo")
    public void createUser() {
        user = new User(testEmail, testPassword, testName);
        response = given().header("Content-type", "application/json").body(user).post(userRegistrationEndpoint);
        userAuthInfo = response.body().as(UserAuthInfo.class);
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }


}
