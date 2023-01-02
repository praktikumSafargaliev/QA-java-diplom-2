import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class AuthorisationUserTest {

    private User user;
    private UserAuthInfo userAuthInfo;
    private Response response;
    private final String baseURI = "https://stellarburgers.nomoreparties.site";
    private final String userRegistrationEndpoint = "/api/auth/register/";
    private final String userAuthorisationEndpoint = "/api/auth/login/";
    private final String userDataEndpoint = "/api/auth/user/";
    private final String testEmail = "autotestruslan@ya.ru";
    private final String testPassword = "autotest";
    private final String testName = "Ruslan";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseURI;
        user = new User(testEmail, testPassword, testName);
        createUser();
    }


    @Test
    @DisplayName("Авторизация существующим пользователем")
    public void authorisationExistingUserTest() {
        authorisationUser().then().statusCode(200)
                .and().assertThat().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Авторизация с неправильным логином")
    public void authorisationWithWrongLoginTest() {
        user = new User("wrongemailfortest@ya.ru", testPassword, testName);
        authorisationUser().then().statusCode(401)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем")
    public void authorisationWithWrongPasswordTest() {
        user = new User(testEmail, "wrongpasswordfortest", testName);
        authorisationUser().then().statusCode(401)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("email or password are incorrect"));
    }



    @After
    public void deleteUser() {
        if (getAccessToken() != null) {
            given().header("Authorization", getAccessToken()).delete(userDataEndpoint);
        }
    }

    @Step("Создаём пользователя и кладём тело ответа в класс UserAuthInfo")
    public void createUser() {
        response = given().header("Content-type", "application/json").body(user).post(userRegistrationEndpoint);
        userAuthInfo = response.body().as(UserAuthInfo.class);
    }

    @Step("Авторизация пользователем")
    public Response authorisationUser() {
        response = given().header("Content-type", "application/json").body(user).post(userAuthorisationEndpoint);
        return response;
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }

}
