package user;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static constants.Constant.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class AuthorisationUserTest {

    private User user;
    private UserAuthInfo userAuthInfo;
    private Response response;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        user = new User(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);
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
        user = new User("wrongemailfortest@ya.ru", TEST_PASSWORD, TEST_NAME);
        authorisationUser().then().statusCode(401)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем")
    public void authorisationWithWrongPasswordTest() {
        user = new User(TEST_EMAIL, "wrongpasswordfortest", TEST_NAME);
        authorisationUser().then().statusCode(401)
                .and().assertThat().body("success", equalTo(false), "message", equalTo("email or password are incorrect"));
    }



    @After
    @DisplayName("Удаляем пользователя")
    public void deleteUser() {
        if (getAccessToken() != null) {
            given().header("Authorization", getAccessToken()).delete(USER_DATA_ENDPOINT);
        }
    }

    @Step("Создаём пользователя и кладём тело ответа в класс UserAuthInfo")
    public void createUser() {
        response = given().header("Content-type", "application/json").body(user).post(USER_REGISTRATION_ENDPOINT);
        userAuthInfo = response.body().as(UserAuthInfo.class);
    }

    @Step("Авторизация пользователем")
    public Response authorisationUser() {
        response = given().header("Content-type", "application/json").body(user).post(USER_AUTHORISATION_ENDPOINT);
        return response;
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }

}
