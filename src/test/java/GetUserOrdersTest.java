import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserOrdersTest {

    private User user;
    private UserAuthInfo userAuthInfo;
    private Response response;
    private final String baseURI = "https://stellarburgers.nomoreparties.site";
    private final String userRegistrationEndpoint = "/api/auth/register/";
    private final String userDataEndpoint = "/api/auth/user/";
    private final String userOrdersEndpoint = "/api/orders/";
    private final String testEmail = "autotestruslan@ya.ru";
    private final String testPassword = "autotest";
    private final String testName = "Ruslan";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseURI;
    }

    @Test
    @DisplayName("Получаем список заказов авторизованного пользователя")
    public void getUserOrdersWithTokenTest() {
        createUser();
        getUserOrdersWithToken()
                .then().statusCode(200)
                .and().assertThat().body("success", equalTo(true), "$", hasKey("orders"));
        deleteUser();
    }

    @Test
    @DisplayName("Получаем список заказов неавторизованного пользователя")
    public void getUserOrdersWithoutTokenTest() {
        getUserOrdersWithoutToken()
                .then().statusCode(401)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("You should be authorised"));
    }



    @Step("Создаём пользователя и кладём тело ответа в класс UserAuthInfo")
    public void createUser() {
        user = new User(testEmail, testPassword, testName);
        response = given().header("Content-type", "application/json").body(user).post(userRegistrationEndpoint);
        userAuthInfo = response.body().as(UserAuthInfo.class);
    }

    @Step("Удаляем пользователя")
    public void deleteUser() {
        if (getAccessToken() != null) {
            given().header("Authorization", getAccessToken()).delete(userDataEndpoint);
        }
    }

    @Step("Получаем список заказов пользователя, передав токен авторизации")
    public Response getUserOrdersWithToken() {
        response = given().headers("Authorization", getAccessToken()).get(userOrdersEndpoint);
        return response;
    }

    @Step("Получаем список заказов пользователя, без передачи токена авторизации")
    public Response getUserOrdersWithoutToken() {
        response = given().get(userOrdersEndpoint);
        return response;
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }

}
