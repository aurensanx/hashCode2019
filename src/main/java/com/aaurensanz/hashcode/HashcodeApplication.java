package com.aaurensanz.hashcode;

import com.aaurensanz.hashcode.utils.Photo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
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

            int totalScore = 0;
            for (String file : FILE) {
                List<Photo> photoList = readPhotos(file);
                photoList = groupVerticalPhotos(photoList);
                HashMap<String, Integer> tagMap = getTagMap(photoList);
                setInterestScore(photoList, tagMap);

                photoList = runSolution(photoList);
                totalScore += writePhotos(photoList, file);
            }

            System.out.println("Total Score: " + totalScore);

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
        for (int i = 0; i < verticalPhotos.size() - 1; i++) {

            auxVerticalPhoto = new Photo();
            auxVerticalPhoto.setId(verticalPhotos.get(i).getId() + " " + verticalPhotos.get(i + 1).getId());

            Set<String> tagSet = new LinkedHashSet<>(verticalPhotos.get(i).getTags());
            tagSet.addAll(verticalPhotos.get(i + 1).getTags());
            auxVerticalPhoto.setTags(new ArrayList<>(tagSet));

            auxVerticalPhoto.setNumberOfTags(auxVerticalPhoto.getTags().size());

            groupedPhotos.add(auxVerticalPhoto);
            i++;
        }

        return groupedPhotos;
    }

    private static HashMap<String, Integer> getTagMap(List<Photo> photoList) {
        HashMap<String, Integer> tagMap = new HashMap<>();

        for (Photo photo : photoList) {
            for (String tag : photo.getTags()) {
                tagMap.merge(tag, 1, Integer::sum);
            }
        }
        return tagMap;
    }

    private static void setInterestScore(List<Photo> photoList, HashMap<String, Integer> tagMap) {
        int interestScore;
        for (Photo photo : photoList) {
            interestScore = 0;
            for (String tag : photo.getTags()) {
                interestScore += tagMap.get(tag);
            }
            photo.setInterestScore(interestScore);
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
//        photoList.sort(Comparator.comparing(Photo::getInterestScore).reversed());

        Map<Integer, List<Photo>> photoMapByTagNumber = photoList.stream().collect(groupingBy(Photo::getNumberOfTags));

        for (Map.Entry<Integer, List<Photo>> entry : photoMapByTagNumber.entrySet()) {

            List<Photo> orderedPhotoList = entry.getValue().stream().sorted(Comparator.comparing(Photo::getInterestScore)).collect(toList());
            orderedPhotoList.sort(Comparator.comparing(Photo::getInterestScore).reversed());

//            List<Photo> orderedPhotoList = entry.getValue();

//            System.out.println("Number of tags: " + entry.getKey() + ", Number of photos: " + entry.getValue().size() + ", Number of groups by tag number: " + photoMapByTagNumber.size());
            Photo auxPhoto;

            for (int i = 0; i < orderedPhotoList.size() - 1; i++) {
                Integer currentScore = getScore(orderedPhotoList.get(i), orderedPhotoList.get(i + 1));

                for (int j = 2; j < orderedPhotoList.size() - 100; j++) {
                    j += 100;
                    Integer score = getScore(orderedPhotoList.get(i), orderedPhotoList.get(j));
                    if (score > currentScore) {
                        auxPhoto = orderedPhotoList.get(i + 1);
                        orderedPhotoList.set(i + 1, orderedPhotoList.get(j));
                        orderedPhotoList.set(j, auxPhoto);
                    }
                }
            }

            finalPhotoList.addAll(orderedPhotoList);
        }

        return photoList;
    }

    private static Integer getTotalScore(List<Photo> photoList) {
        Integer score = 0;
        for (int i = 1; i < photoList.size(); i++) {
            score += getScore(photoList.get(i - 1), photoList.get(i));
        }
        return score;
    }

    private static Integer getScore(Photo photo1, Photo photo2) {

        Integer intersection = 0;
        Integer inFirst = 0;
        Integer inSecond = photo2.getTags().size();
        for (String tag : photo1.getTags()) {
            if (photo2.getTags().indexOf(tag) > -1) {
                intersection++;
                inSecond--;
            } else {
                inFirst++;
            }
        }
        return Math.min(intersection, Math.min(inFirst, inSecond));
    }


}
