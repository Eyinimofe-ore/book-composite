package com.example.bookcomposite.integration;

import com.example.bookcomposite.model.Book;
import com.example.bookcomposite.model.Recommendation;
import com.example.bookcomposite.model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookCompositeIntegration {

    private final RestTemplate restTemplate;
    private final String bookServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public BookCompositeIntegration(RestTemplate restTemplate,
                                    @Value("${book.service.url}") String bookServiceUrl,
                                    @Value("${recommendation.service.url}") String recommendationServiceUrl,
                                    @Value("${review.service.url}") String reviewServiceUrl) {
        this.restTemplate = restTemplate;
        this.bookServiceUrl = bookServiceUrl;
        this.recommendationServiceUrl = recommendationServiceUrl;
        this.reviewServiceUrl = reviewServiceUrl;
    }

    public List<Book> getBooks() {
        try {
            Book[] books = restTemplate.getForObject(bookServiceUrl, Book[].class);
            return books != null ? Arrays.asList(books) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

    public Book getBookById(int bookId) {
        try {
            return restTemplate.getForObject(
                    bookServiceUrl + "/" + bookId,
                    Book.class
            );
        } catch (HttpClientErrorException.NotFound nf) {
            return null;
        }
    }

    public List<Recommendation> getRecommendationsForBook(int bookId) {
        try {
            Recommendation[] recs = restTemplate.getForObject(
                    recommendationServiceUrl + "?productId=" + bookId,
                    Recommendation[].class);
            if (recs == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(recs)
                    .filter(r -> r.getProductId().equals(bookId))
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    public List<Review> getReviewsForBook(int bookId) {
        try {
            Review[] revs = restTemplate.getForObject(
                    reviewServiceUrl + "/" + bookId,
                    Review[].class);
            return revs != null
                    ? Arrays.asList(revs)
                    : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
