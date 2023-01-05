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

public class ChangeUserDataTest {

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
    @DisplayName("Изменяем имя пользователя")
    public void changeUserNameTest() {
        user.setName("ExpectedUserName");
        changeUserData().then().statusCode(200).and().body("user.name", equalTo("ExpectedUserName"));
    }

    @Test
    @DisplayName("Изменяем почту пользователя")
    public void changeUserEmailTest() {
        user.setEmail("expectedmail@ya.ru");
        changeUserData().then().statusCode(200).and().body("user.email", equalTo("expectedmail@ya.ru"));
    }

    @Test
    @DisplayName("Изменяем почту пользователя на адрес, который уже используется")
    public void changeUserEmailToExistingEmailTest() {
        String createdUserToken = userAuthInfo.getAccessToken();
        user.setEmail("existingemail@ya.ru");
        createUser();
        given().headers("Authorization", createdUserToken,  "Content-type", "application/json").body(user).patch(USER_DATA_ENDPOINT)
                .then().statusCode(403)
                .and().body("success", equalTo(false), "message", equalTo("User with such email already exists")); // изменяем данные созданного в Before пользователя
        given().header("Authorization", createdUserToken).delete(USER_DATA_ENDPOINT); // удаляем созданного в Before пользователя, второй пользователь удалится после теста
    }

    @Test
    @DisplayName("Пытаемся изменить имя пользователя, не отправляя токен авторизации")
    public void changeUserNameTestWithoutTokenTest() {
        user.setName("ExpectedUserName");
        changeUserDataWithoutToken()
                .then().statusCode(401)
                .and().body("success", equalTo(false), "message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Пытаемся изменить почту пользователя, не отправляя токен авторизации")
    public void changeUserEmailTestWithoutTokenTest() {
        user.setEmail("expectedmail@ya.ru");
        changeUserDataWithoutToken()
                .then().statusCode(401)
                .and().body("success", equalTo(false), "message", equalTo("You should be authorised"));
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

    @Step("Отправляем PATCH запрос с токеном авторизации на изменение данных пользователя")
    public Response changeUserData() {
            response = given().headers("Authorization", getAccessToken(),  "Content-type", "application/json").body(user).patch(USER_DATA_ENDPOINT);
            return response;
    }

    @Step("Отправляем PATCH запрос БЕЗ токена авторизации на изменение данных пользователя")
    public Response changeUserDataWithoutToken() {
        response = given().headers("Content-type", "application/json").body(user).patch(USER_DATA_ENDPOINT);
        return response;
    }

    @Step("Получаем токен авторизации")
    public String getAccessToken() {
        return userAuthInfo.getAccessToken();
    }


}
