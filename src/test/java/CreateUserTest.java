import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserTest {

    private User user;
    private UserAuthInfo userAuthInfo;
    private Response response;
    private final String baseURI = "https://stellarburgers.nomoreparties.site";
    private final String userRegistrationEndpoint = "/api/auth/register/";
    private final String authUserEndpoint = "/api/auth/user/";
    private final String testEmail = "autotestruslan@ya.ru";
    private final String testPassword = "autotest";
    private final String testName = "Ruslan";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseURI;
        user = new User(testEmail, testPassword, testName);
    }

    @After
    public void deleteUser() {
        if (getAccessToken() != null) {
            given().header("Authorization", getAccessToken()).delete(authUserEndpoint);
        }
    }

    @Test
    @DisplayName("Проверяем создание пользователя")
    public void createUserTest() {
        createUser().then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Проверяем создание существующего пользователя")
    public void createExistingUserTest() {
        createUser();
        given().header("Content-type", "application/json").body(user).post(userRegistrationEndpoint).then().statusCode(403)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Проверяем создание пользователя без заполненного обязательного поля: email")
    public void createUserWithEmptyEmail() {
        user = new User("", testPassword, testName);
        createUser().then().statusCode(403)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Проверяем создание пользователя без заполненного обязательного поля: password")
    public void createUserWithEmptyPassword() {
        user = new User(testEmail, "", testName);
        createUser().then().statusCode(403)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Проверяем создание пользователя без заполненного обязательного поля: name")
    public void createUserWithEmptyName() {
        user = new User(testEmail, testPassword, "");
        createUser().then().statusCode(403)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("Email, password and name are required fields"));
    }

    @Step("Создаём пользователя и кладём тело ответа в класс UserAuthInfo")
    public Response createUser() {
        response = given().header("Content-type", "application/json").body(user).post(userRegistrationEndpoint);
        userAuthInfo = response.body().as(UserAuthInfo.class);
        return response;
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }

}
