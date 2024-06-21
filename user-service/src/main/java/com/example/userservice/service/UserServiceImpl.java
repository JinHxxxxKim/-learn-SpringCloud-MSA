package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RestTemplate restTemplate;

    private final Environment env;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername : {} ", username);
        UserEntity userEntity = userRepository.findByEmail(username);
        log.info("userEntity : {} ", userEntity);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));

        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null) {
            throw new UsernameNotFoundException("User Not Found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        // 임시 주문 데이터 저장
//        List<ResponseOrder> orders = new ArrayList<>();
//        userDto.setOrders(orders);

        // Using RestTemplate
        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders"; // 하드코딩
        orderUrl = String.format(env.getProperty("order_service.url"), userId); // 설정 파일로부터 url 값 가져오기

        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ResponseOrder>>() {
        });

        userDto.setOrders(orderListResponse.getBody());
        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        ModelMapper mapper = new ModelMapper();
        return mapper.map(userEntity, UserDto.class);
    }
}
