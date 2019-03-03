package com.aaurensanz.hashcode;

import com.aaurensanz.hashcode.utils.Photo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class HashcodeApplication {

    private static final String FILE_PATH_IN = "sampledata/in/";
    private static final String FILE_PATH_OUT = "sampledata/out/";
    private static final String[] FILE = {"a_example", "b_lovely_landscapes", "c_memorable_moments", "d_pet_pictures", "e_shiny_selfies"};
    private static final String FILE_EXTENSION = ".txt";


    public static void main(String[] args) {
        SpringApplication.run(HashcodeApplication.class, args);
        try {
            Date initDate = new Date();
            System.out.println(initDate);
            int totalScore = 0;
            for (String file : FILE) {
                List<Photo> photoList = readPhotos(file);
                photoList = groupVerticalPhotos(photoList);
                HashMap<String, Integer> tagMapOcurrences = getTagMapOcurrences(photoList);
                HashMap<String, Integer> tagMapIndex = getTagMapIndex(photoList);
                setInterestScore(photoList, tagMapOcurrences, tagMapIndex);

                photoList = runSolution(photoList);
                totalScore += writePhotos(photoList, file);
            }

            System.out.println("Total Score: " + totalScore);
            Date endDate = new Date();
            System.out.println(endDate);
            System.out.println((endDate.getTime() - initDate.getTime()) / 1000 + " seconds elapsed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Photo> groupVerticalPhotos(List<Photo> photoList) {

        List<Photo> groupedPhotos = new ArrayList<>();
        List<Photo> verticalPhotos = new ArrayList<>();
        Photo auxVerticalPhoto;

        for (Photo photo : photoList) {
            if (photo.getOrientation() == 'V') {
                verticalPhotos.add(photo);
            } else {
                groupedPhotos.add(photo);
            }
        }

        verticalPhotos = verticalPhotos.stream().sorted(Comparator.comparing(Photo::getNumberOfTags)).collect(toList());
        List<Photo> auxVerticalPhotos = new ArrayList<>();

        for (int i = 0; i < verticalPhotos.size() / 2; i++) {
            auxVerticalPhotos.add(verticalPhotos.get(i));
            auxVerticalPhotos.add(verticalPhotos.get(verticalPhotos.size() - i - 1));
        }
        verticalPhotos = auxVerticalPhotos;

        int WINDOW_SIZE = 25;

        Photo auxPhoto;

        for (int i = 0; i < verticalPhotos.size() - 1; i++) {

            int numberOftags;

            int currentNumberOfTags = mergeTags(verticalPhotos.get(i), verticalPhotos.get(i + 1)).size();
            int limit = Math.min(verticalPhotos.size(), WINDOW_SIZE + i);

            for (int j = i + 2; j < limit; j++) {
                numberOftags = mergeTags(verticalPhotos.get(i), verticalPhotos.get(j)).size();
                if (numberOftags > currentNumberOfTags) {
                    auxPhoto = verticalPhotos.get(i + 1);
                    verticalPhotos.set(i + 1, verticalPhotos.get(j));
                    verticalPhotos.set(j, auxPhoto);
                    currentNumberOfTags = numberOftags;
                }
            }
        }

        for (int i = 0; i < verticalPhotos.size() - 1; i++) {

            auxVerticalPhoto = new Photo();
            auxVerticalPhoto.setId(verticalPhotos.get(i).getId() + " " + verticalPhotos.get(i + 1).getId());
            auxVerticalPhoto.setTags(mergeTags(verticalPhotos.get(i), verticalPhotos.get(i + 1)));
            auxVerticalPhoto.setNumberOfTags(auxVerticalPhoto.getTags().size());

            groupedPhotos.add(auxVerticalPhoto);
            i++;
        }

        return groupedPhotos;
    }

    private static List<String> mergeTags(Photo photo1, Photo photo2) {
        Set<String> tagSet = new LinkedHashSet<>(photo1.getTags());
        tagSet.addAll(photo2.getTags());
        return new ArrayList<>(tagSet);
    }

    private static HashMap<String, Integer> getTagMapIndex(List<Photo> photoList) {
        HashMap<String, Integer> tagMap = new HashMap<>();
        int count = 0;

        for (Photo photo : photoList) {
            for (String tag : photo.getTags()) {
                if (tagMap.get(tag) == null) {
                    tagMap.put(tag, count++);
                }
            }
        }
        return tagMap;
    }

    private static HashMap<String, Integer> getTagMapOcurrences(List<Photo> photoList) {
        HashMap<String, Integer> tagMap = new HashMap<>();

        for (Photo photo : photoList) {
            for (String tag : photo.getTags()) {
                tagMap.merge(tag, 1, Integer::sum);
            }
        }
        return tagMap;
    }

    private static void setInterestScore(List<Photo> photoList, HashMap<String, Integer> tagMapOcurrences, HashMap<String, Integer> tagMapIndexes) {
        List<Integer> tagIndexes;
        int interestScore;
        for (Photo photo : photoList) {
            interestScore = 0;
            tagIndexes = new ArrayList<>();
            for (String tag : photo.getTags()) {
                interestScore += tagMapOcurrences.get(tag);
                tagIndexes.add(tagMapIndexes.get(tag));
            }
            photo.setInterestScore(interestScore);
            photo.setTagIndexes(tagIndexes);
        }
    }

    private static List<Photo> readPhotos(String file) throws IOException {
        // LEER FICHERO
        BufferedReader br = new BufferedReader(new FileReader(FILE_PATH_IN + file + FILE_EXTENSION));

        // PRIMERA LINEA
        Integer NUMBER_OF_PHOTOS = Integer.parseInt(br.readLine());

        List<Photo> photoList = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_PHOTOS; i++) {

            String[] photoString = br.readLine().split(" ");
            Photo photo = new Photo();
            photo.setId("" + i);
            photo.setOrientation(photoString[0].charAt(0));
            photo.setNumberOfTags(Integer.parseInt(photoString[1]));
            for (int j = 2; j < photo.getNumberOfTags() + 2; j++) {
                photo.getTags().add(photoString[j]);
            }
            photoList.add(photo);
        }

        br.close();

        return photoList;
    }

    private static int writePhotos(List<Photo> groupedPhotoList, String file) throws IOException {

        //ESCRIBIR FICHERO
        BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH_OUT + file + FILE_EXTENSION));

        bw.write("" + groupedPhotoList.size());

        Integer score = 0;
        bw.newLine();
        bw.write(groupedPhotoList.get(0).getId());

        for (int i = 1; i < groupedPhotoList.size(); i++) {
            score += getScore(groupedPhotoList.get(i - 1), groupedPhotoList.get(i));
            bw.newLine();
            bw.write(groupedPhotoList.get(i).getId());
        }

        System.out.print("Score: " + score + "\n");

        // CERRAR
        bw.close();

        return score;
    }

    private static List<Photo> runSolution(List<Photo> photoList) {

        List<Photo> finalPhotoList = new ArrayList<>();
        int WINDOW_SIZE = Math.max(photoList.size() / 1000, 1);
//        int WINDOW_SIZE = 10;
        photoList.sort(Comparator.comparing(Photo::getInterestScore).reversed());

        List<Photo> orderedPhotoList = photoList;


//        Map<Integer, List<Photo>> photoMapByTagNumber = photoList.stream().collect(groupingBy(Photo::getNumberOfTags));

//        for (Map.Entry<Integer, List<Photo>> entry : photoMapByTagNumber.entrySet()) {

//            List<Photo> orderedPhotoList = entry.getValue().stream().sorted(Comparator.comparing(Photo::getInterestScore)).collect(toList());
//            orderedPhotoList.sort(Comparator.comparing(Photo::getInterestScore).reversed());

//        List<Photo> photoListAux = new ArrayList<>();
////
//        int photoFragments = Math.min(orderedPhotoList.size(), 2);
//
////        System.out.println("Photo fragments: " + photoFragments);
//
//        for (int i = 0; i < orderedPhotoList.size() / photoFragments; i++) {
//            photoListAux.add(orderedPhotoList.get(i));
//            photoListAux.add(orderedPhotoList.get(i + orderedPhotoList.size() - i - 1));
//        }
//
//        orderedPhotoList = photoListAux;

        Photo auxPhoto;

        for (int i = 0; i < orderedPhotoList.size() - 1; i++) {
            Integer currentScore = getScore(orderedPhotoList.get(i), orderedPhotoList.get(i + 1));
            Integer score;

            int limit = Math.min(orderedPhotoList.size(), WINDOW_SIZE + i);
            for (int j = i + 2; j < limit; j++) {
                score = getScore(orderedPhotoList.get(i), orderedPhotoList.get(j));
                if (score > currentScore) {
                    auxPhoto = orderedPhotoList.get(i + 1);
                    orderedPhotoList.set(i + 1, orderedPhotoList.get(j));
                    orderedPhotoList.set(j, auxPhoto);

                    currentScore = score;
                }
            }
        }

        finalPhotoList.addAll(orderedPhotoList);

        return finalPhotoList;
    }

    private static Integer getScore(Photo photo1, Photo photo2) {

        Integer intersection = 0;
        Integer inFirst = 0;
        Integer inSecond = photo2.getTagIndexes().size();
        for (Integer tag : photo1.getTagIndexes()) {
            if (photo2.getTagIndexes().indexOf(tag) > -1) {
                intersection++;
                inSecond--;
            } else {
                inFirst++;
            }
        }
        return Math.min(intersection, Math.min(inFirst, inSecond));
    }


}
