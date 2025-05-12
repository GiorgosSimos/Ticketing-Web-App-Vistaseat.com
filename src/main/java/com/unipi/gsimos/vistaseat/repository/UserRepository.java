package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(UserRole role);

    /**
     * Retrieves a paginated list of users filtered by their role.
     *
     * <p>This method leverages the {@link Pageable} interface to support pagination,
     * which is a technique used to break large datasets into manageable chunks (pages).
     * The pagination parameters include:
     * <ul>
     *     <li><b>Page Number</b>: The index of the page to retrieve (starting from 0).</li>
     *     <li><b>Page Size</b>: The number of items to include on each page.</li>
     *     <li><b>Offset</b>: The index from which records are fetched, calculated as
     *     <code>(pageNumber - 1) * pageSize</code>.</li>
     * </ul>
     *
     * <p>For example, requesting page 2 with a page size of 10 will result in an offset of 10,
     * meaning items from the 11th onward will be returned.
     *
     * <p>The result is a {@link org.springframework.data.domain.Page} of {@link User} objects,
     * containing both the filtered users and metadata like total pages and total elements.
     *
     * @param role the role to filter users by
     * @param pageable the pagination information including page number and page size
     * @return a {@link Page} of users with the specified role
     */
    Page<User> findAllByRole(UserRole role, Pageable pageable);

    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    List<User> findTop10ByRoleOrderByIdDesc(UserRole role);

}
