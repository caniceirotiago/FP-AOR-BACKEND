package aor.fpbackend.bean;

import aor.fpbackend.dao.InterestDao;
import aor.fpbackend.dao.UserDao;
import aor.fpbackend.dto.Authentication.AuthUserDto;
import aor.fpbackend.dto.Interest.InterestAddDto;
import aor.fpbackend.dto.Interest.InterestGetDto;
import aor.fpbackend.dto.Interest.InterestRemoveDto;
import aor.fpbackend.entity.InterestEntity;
import aor.fpbackend.entity.UserEntity;
import aor.fpbackend.enums.InterestTypeEnum;
import aor.fpbackend.exception.DuplicatedAttributeException;
import aor.fpbackend.exception.EntityNotFoundException;
import aor.fpbackend.exception.UserNotFoundException;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterestBeanTest {

    @InjectMocks
    private InterestBean interestBean;

    @Mock
    private InterestDao interestDao;

    @Mock
    private UserDao userDao;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddInterest_Success() throws Exception {
        InterestAddDto interestAddDto = new InterestAddDto("Reading", InterestTypeEnum.KNOWLEDGE_AREAS);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        InterestEntity interestEntity = new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS);
        UserEntity userEntity = new UserEntity();

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(interestDao.findInterestByName(interestAddDto.getName())).thenReturn(interestEntity);

        interestBean.addInterest(interestAddDto, securityContext);

        verify(interestDao, times(1)).findInterestByName(interestAddDto.getName());
        verify(userDao, times(1)).findUserById(authUserDto.getUserId());
        assertTrue(userEntity.getUserInterests().contains(interestEntity));
    }

    @Test
    void testAddInterest_ThrowsDuplicatedAttributeException() {
        InterestAddDto interestAddDto = new InterestAddDto("Reading", InterestTypeEnum.KNOWLEDGE_AREAS);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        InterestEntity interestEntity = new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS);
        UserEntity userEntity = new UserEntity();
        userEntity.setUserInterests(new HashSet<>(Collections.singletonList(interestEntity)));

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(interestDao.findInterestByName(interestAddDto.getName())).thenReturn(interestEntity);

        assertThrows(DuplicatedAttributeException.class, () -> {
            interestBean.addInterest(interestAddDto, securityContext);
        });
    }

    @Test
    void testGetInterests_Success() {
        List<InterestEntity> interestEntities = Arrays.asList(new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS));
        when(interestDao.getAllInterests()).thenReturn(interestEntities);

        List<InterestGetDto> interests = interestBean.getInterests();

        assertEquals(1, interests.size());
        assertEquals("Reading", interests.get(0).getName());
    }

    @Test
    void testGetInterestsByUser_Success() {
        String username = "username";
        List<InterestEntity> interestEntities = Arrays.asList(new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS));
        when(interestDao.getInterestsByUsername(username)).thenReturn(interestEntities);

        List<InterestGetDto> interests = interestBean.getInterestsByUser(username);

        assertEquals(1, interests.size());
        assertEquals("Reading", interests.get(0).getName());
    }

    @Test
    void testGetInterestsByFirstLetter_Success() {
        String firstLetter = "R";
        List<InterestEntity> interestEntities = Arrays.asList(new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS));
        when(interestDao.getInterestsByFirstLetter("r")).thenReturn(interestEntities);

        List<InterestGetDto> interests = interestBean.getInterestsByFirstLetter(firstLetter);

        assertEquals(1, interests.size());
        assertEquals("Reading", interests.get(0).getName());
    }

    @Test
    void testRemoveInterest_Success() throws Exception {
        InterestRemoveDto interestRemoveDto = new InterestRemoveDto(1L);
        AuthUserDto authUserDto = new AuthUserDto(1L, 1L, new HashSet<>(), "token", 1L, "username");
        InterestEntity interestEntity = new InterestEntity("Reading", InterestTypeEnum.KNOWLEDGE_AREAS);
        UserEntity userEntity = new UserEntity();
        userEntity.setUserInterests(new HashSet<>(Collections.singletonList(interestEntity)));

        when(securityContext.getUserPrincipal()).thenReturn(authUserDto);
        when(userDao.findUserById(authUserDto.getUserId())).thenReturn(userEntity);
        when(interestDao.findInterestById(interestRemoveDto.getId())).thenReturn(interestEntity);

        interestBean.removeInterest(interestRemoveDto, securityContext);

        assertFalse(userEntity.getUserInterests().contains(interestEntity));
    }

    @Test
    void testGetEnumListInterestTypes_Success() {
        List<InterestTypeEnum> interestTypes = interestBean.getEnumListInterestTypes();

        assertEquals(InterestTypeEnum.values().length, interestTypes.size());
        assertTrue(interestTypes.contains(InterestTypeEnum.KNOWLEDGE_AREAS));
        assertTrue(interestTypes.contains(InterestTypeEnum.KNOWLEDGE_AREAS));
    }



}
