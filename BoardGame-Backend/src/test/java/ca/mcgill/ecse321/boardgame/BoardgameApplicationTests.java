package ca.mcgill.ecse321.boardgame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "BoardGame App API", version = "3.0.0"))
@SpringBootTest
class BoardgameApplicationTests {

	@Test
	void contextLoads() {
	}

}
