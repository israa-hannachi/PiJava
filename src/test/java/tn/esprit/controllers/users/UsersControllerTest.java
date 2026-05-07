package tn.esprit.controllers.users;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tn.esprit.entities.users.Users;
import tn.esprit.services.users.UsersService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UsersControllerTest {

    @Test
    void hashPasswordShouldProduceExpectedSha256() {
        String hash = UsersController.hashPassword("Password123");
        assertEquals("008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601", hash);
    }

    @Test
    void validerUserShouldReturnNullWhenUserIsValid() throws Exception {
        UsersController controller = new UsersController();
        Users user = validUser();

        String result = invokeValiderUser(controller, user);

        assertNull(result);
    }

    @Test
    void validerUserShouldRejectInvalidEmail() throws Exception {
        UsersController controller = new UsersController();
        Users user = validUser();
        user.setEmail("invalid-email");

        String result = invokeValiderUser(controller, user);

        assertNotNull(result);
        assertTrue(result.contains("L'email est invalide"));
    }

    @Test
    void changePasswordShouldRejectWeakPasswordsBeforeServiceCall() {
        UsersController controller = new UsersController();

        String shortResult = controller.changePassword(1, "Ab1");
        String noUpperCaseResult = controller.changePassword(1, "password1");
        String noDigitResult = controller.changePassword(1, "Password");

        assertNotNull(shortResult);
        assertTrue(shortResult.contains("au moins 6"));

        assertNotNull(noUpperCaseResult);
        assertTrue(noUpperCaseResult.contains("majuscule"));

        assertNotNull(noDigitResult);
        assertTrue(noDigitResult.contains("chiffre"));
    }

    @Test
    void changePasswordShouldHashAndDelegateToService() throws Exception {
        UsersController controller = new UsersController();
        UsersService serviceMock = Mockito.mock(UsersService.class);

        Field serviceField = UsersController.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(controller, serviceMock);

        String rawPassword = "Password1";
        String result = controller.changePassword(7, rawPassword);

        assertNull(result);
        verify(serviceMock, times(1)).updatePassword(eq(7), anyString());
        verify(serviceMock, times(1)).updatePassword(eq(7), eq(UsersController.hashPassword(rawPassword)));
    }

    private static Users validUser() {
        Users user = new Users();
        user.setFirstName("Ali");
        user.setLastName("Ben Salah");
        user.setEmail("ali@example.com");
        user.setPassword("Password1");
        user.setRole("admin");
        user.setProfession("Engineer");
        user.setExperienceLevel("Senior");
        user.setStatut("ACTIF");
        return user;
    }

    private static String invokeValiderUser(UsersController controller, Users user) throws Exception {
        Method method = UsersController.class.getDeclaredMethod("validerUser", Users.class);
        method.setAccessible(true);
        Object result = method.invoke(controller, user);
        return (String) result;
    }
}
