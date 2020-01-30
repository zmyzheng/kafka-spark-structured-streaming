package io.zmyzheng;

/**
 * @Author: Mingyang Zheng
 * @Date: 2020-01-29 20:07
 */
public class App {
    public static void main(String[] args) {
        System.out.println("haha");

//        BufferedWriter bufferedWriter = null;
//
//        try {
//            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("start.txt")));
//            bufferedWriter.write("haha");
//            bufferedWriter.newLine();
//            bufferedWriter.flush();
//
//        } catch (IOException e) {
//            System.out.println(e.getCause());
//
//
//        }finally {
//            if (bufferedWriter != null) {
//                try {
//                    bufferedWriter.close();
//                } catch (IOException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//        }







        StreamPipeline streamPipeline = new StreamPipeline();
        streamPipeline.startPipeLine();
    }

}
