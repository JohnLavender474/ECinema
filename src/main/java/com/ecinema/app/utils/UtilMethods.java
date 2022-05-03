package com.ecinema.app.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Util methods.
 */
public class UtilMethods {

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * Gets random.
     *
     * @return the random
     */
    public static Random getRandom() {
        return random;
    }

    /**
     * Random int between int.
     *
     * @param min the min
     * @param max the max
     * @return the int
     */
    public static int randomIntBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Gets delimiter line.
     *
     * @return the delimiter line
     */
    public static String getDelimiterLine() {
        return "---------------------------------------------------------------------------------------------";
    }

    /**
     * Local date time overlap boolean.
     *
     * @param start1 the start 1
     * @param end1   the end 1
     * @param start2 the start 2
     * @param end2   the end 2
     * @return the boolean
     */
    public static boolean localDateTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                               LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * Convert list to page.
     *
     * @param <T>      the type parameter
     * @param list     the list
     * @param pageable the pageable
     * @return the page
     */
    public static <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    /**
     * Find all that collection contains if any list.
     *
     * @param <T>                        the type parameter
     * @param checkIfThisContains        the check if this contains
     * @param checkIfOtherContainsOfThis the check if other contains of this
     * @return the list
     */
    public static <T> List<T> findAllThatCollectionContainsIfAny(
            Collection<T> checkIfThisContains, Collection<T> checkIfOtherContainsOfThis) {
        List<T> list = new ArrayList<>();
        for (T t : checkIfOtherContainsOfThis) {
            if (checkIfThisContains.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Find all keys that map contains if any list.
     *
     * @param <T>        the type parameter
     * @param map        the map
     * @param collection the collection
     * @return the list
     */
    public static <T> List<T> findAllKeysThatMapContainsIfAny(Map<T, ?> map, Collection<T> collection) {
        return findAllThatCollectionContainsIfAny(map.keySet(), collection);
    }

    /**
     * Is alphabetical only boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public static boolean isAlphabeticalOnly(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is digits only boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public static boolean isDigitsOnly(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is alpha and digits only boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public static boolean isAlphaAndDigitsOnly(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Num special chars int.
     *
     * @param s the s
     * @return the int
     */
    public static int numSpecialChars(String s) {
        int num = 0;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isAlphabetic(c)) {
                num++;
            }
        }
        return num;
    }

    /**
     * Remove substrings string.
     *
     * @param s       the s
     * @param targets the targets
     * @return the string
     */
    public static String removeSubstrings(String s, String... targets) {
        String str = "";
        for (String target : targets) {
            str = s.replace(target, "");
        }
        return str;
    }

    /**
     * Remove whitespace string.
     *
     * @param s the s
     * @return the string
     */
    public static String removeWhitespace(String s) {
        return s.replaceAll("\\s+", "");
    }

    /**
     * Random date time local date time.
     *
     * @return the local date time
     */
    public static LocalDateTime randomDateTime() {
        return LocalDateTime.of(randomDate(), randomTime());
    }

    /**
     * Random date local date.
     *
     * @return the local date
     */
    public static LocalDate randomDate() {
        long minDay = LocalDate.of(2022, Month.JANUARY, 1).toEpochDay();
        LocalDate localDate = LocalDate.now();
        long maxDay = LocalDate.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth()).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * Random time local time.
     *
     * @return the local time
     */
    public static LocalTime randomTime() {
        int randomHour = ThreadLocalRandom.current().nextInt(0, 23 + 1);
        int randomMinute = ThreadLocalRandom.current().nextInt(0, 59 + 1);
        return LocalTime.of(randomHour, randomMinute);
    }

}
