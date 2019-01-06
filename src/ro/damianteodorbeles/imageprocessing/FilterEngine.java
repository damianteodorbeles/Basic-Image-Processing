package ro.damianteodorbeles.imageprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilterEngine {

	private final int numberOfThreadsToProcessOn = 4;

	public BufferedImage processImage(final BufferedImage image, Filter filter) {
		switch (filter) {
		case BOX_BLUR:
			return applyKernel(image, new KernelFactory().BOX_BLUR());
		case GAUSSIAN_BLUR:
			return applyKernel(image, new KernelFactory().GAUSSIAN_BLUR());
		case EDGE_DETECTION:
			return applyKernel(image, new KernelFactory().EDGE_DETECTION());
		case SHARPEN:
			return applyKernel(image, new KernelFactory().SHARPEN());
		case EMBOSS:
			return applyKernel(image, new KernelFactory().EMBOSS());
		case RED_COLORING:
			return setColorFilter(image, ColorFilter.RED);
		case GREEN_COLORING:
			return setColorFilter(image, ColorFilter.GREEN);
		case BLUE_COLORING:
			return setColorFilter(image, ColorFilter.BLUE);
		case GRAYSCALE:
			return grayscale(image);
		default:
			return image;
		}
	}

	private BufferedImage copyImage(BufferedImage image) {
		BufferedImage returnImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);
		returnImage.getGraphics().drawImage(image, 0, 0, null);
		return returnImage;
	}

	private BufferedImage createBufferedImageFromByteArray(final byte[] pixels, final int imageWidth,
			final int imageHeight, final int type) {
		BufferedImage returnImage = new BufferedImage(imageWidth, imageHeight, type);
		returnImage.setData(
				Raster.createRaster(returnImage.getSampleModel(), new DataBufferByte(pixels, pixels.length), null));
		return returnImage;
	}

	private BufferedImage grayscale(final BufferedImage image) {
		BufferedImage imageToProcess = copyImage(image);
		final int imageWidth = imageToProcess.getWidth();
		final int imageHeight = imageToProcess.getHeight();
		final byte[] pixels = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();

		final byte[] newPixels = new byte[pixels.length];

		ExecutorService grayscaleMultithread = Executors.newFixedThreadPool(numberOfThreadsToProcessOn);
		CompletionService<Void> grayscaleMultithreadCompletition = new ExecutorCompletionService<Void>(
				grayscaleMultithread);

		for (int threadIndex = 0; threadIndex < numberOfThreadsToProcessOn; ++threadIndex) {
			final int indexer = threadIndex;
			grayscaleMultithreadCompletition.submit(new Callable<Void>() {
				public Void call() {
					final int startRow = indexer * (imageHeight / numberOfThreadsToProcessOn);
					final int endRow = (indexer + 1) * (imageHeight / numberOfThreadsToProcessOn);
					final int startCol = 0;
					final int endCol = imageWidth;

					for (int row = startRow; row < endRow; ++row) {
						for (int col = startCol; col < endCol; ++col) {
							int pos = row * 4 * imageWidth + col * 4;

							final float blue = ((int) pixels[pos + 1]) & 0xFF;
							final float green = ((int) pixels[pos + 2]) & 0xFF;
							final float red = ((int) pixels[pos + 3]) & 0xFF;
							byte gray = (byte) (0.299f * red + 0.587f * green + 0.114f * blue);

							newPixels[pos] = (byte) 0xFF;
							for (int pixelNumber = 1; pixelNumber < 4; ++pixelNumber)
								newPixels[pos + pixelNumber] = gray;
						}
					}
					return null;
				}
			});
		}

		for (int threadNumber = 0; threadNumber < numberOfThreadsToProcessOn; ++threadNumber) {
			try {
				grayscaleMultithreadCompletition.take();
			} catch (InterruptedException e) {
				System.out.println("Exception caught while processing the image on multiple threads.");
			}
		}

		return createBufferedImageFromByteArray(newPixels, imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}

	private BufferedImage setColorFilter(final BufferedImage image, final ColorFilter colorFilter) {
		BufferedImage imageToProcess = copyImage(image);
		final int imageWidth = imageToProcess.getWidth();
		final int imageHeight = imageToProcess.getHeight();
		final byte[] pixels = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();

		final byte[] newPixels = new byte[pixels.length];

		int offset;
		if (colorFilter == ColorFilter.BLUE)
			offset = 1;
		else if (colorFilter == ColorFilter.GREEN)
			offset = 2;
		else
			offset = 3;

		ExecutorService setColorFilterMultithread = Executors.newFixedThreadPool(numberOfThreadsToProcessOn);
		CompletionService<Void> setColorFilterMultithreadCompletition = new ExecutorCompletionService<Void>(
				setColorFilterMultithread);

		for (int threadIndex = 0; threadIndex < numberOfThreadsToProcessOn; ++threadIndex) {
			final int indexer = threadIndex;
			setColorFilterMultithreadCompletition.submit(new Callable<Void>() {
				public Void call() {
					final int startRow = indexer * (imageHeight / numberOfThreadsToProcessOn);
					final int endRow = (indexer + 1) * (imageHeight / numberOfThreadsToProcessOn);
					final int startCol = 0;
					final int endCol = imageWidth;

					for (int row = startRow; row < endRow; ++row) {
						for (int col = startCol; col < endCol; ++col) {
							int pos = row * 4 * imageWidth + col * 4;

							int blue = pixels[pos + 1] - 255;
							int green = pixels[pos + 2] - 255;
							int red = pixels[pos + 3] - 255;

							red = Math.max(red, 0);
							red = Math.min(255, red);

							green = Math.max(green, 0);
							green = Math.min(255, green);

							blue = Math.max(blue, 0);
							blue = Math.min(255, blue);

							newPixels[pos] = (byte) 0xFF;
							newPixels[pos + 1] = (byte) blue;
							newPixels[pos + 2] = (byte) green;
							newPixels[pos + 3] = (byte) red;
							newPixels[pos + offset] = pixels[pos + offset];
						}
					}
					return null;
				}
			});
		}

		for (int threadNumber = 0; threadNumber < numberOfThreadsToProcessOn; ++threadNumber) {
			try {
				setColorFilterMultithreadCompletition.take();
			} catch (InterruptedException e) {
				System.out.println("Exception caught while processing the image on multiple threads.");
			}
		}

		return createBufferedImageFromByteArray(newPixels, imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}

	private BufferedImage applyKernel(final BufferedImage image, final IKernel kernel) {
		BufferedImage imageToProcess = copyImage(image);
		final float[] kernelMatrix = kernel.getKernel();
		final int kernelWidth = kernel.getWidth();
		final int kernelHeight = kernel.getHeight();
		final int[] origin = kernel.getOrigin();

		final int imageWidth = imageToProcess.getWidth();
		final int imageHeight = imageToProcess.getHeight();

		if (imageWidth < kernelWidth || imageHeight < kernelHeight) {
			System.out.println("Can't process the image with the given kernel. Kernel too big.");
			return null;
		}

		final byte[] pixels = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();

		final byte[] newPixels = new byte[pixels.length];

		ExecutorService applyKernelMultithread = Executors.newFixedThreadPool(numberOfThreadsToProcessOn);
		CompletionService<Void> applyKernelMultithreadCompletition = new ExecutorCompletionService<Void>(
				applyKernelMultithread);

		for (int threadIndex = 0; threadIndex < numberOfThreadsToProcessOn; ++threadIndex) {
			final int indexer = threadIndex;
			applyKernelMultithreadCompletition.submit(new Callable<Void>() {
				public Void call() {
					final int startRow = indexer * (imageHeight / numberOfThreadsToProcessOn);
					final int endRow = (indexer + 1) * (imageHeight / numberOfThreadsToProcessOn);
					final int startCol = 0;
					final int endCol = imageWidth;

					final int startFromRow = startRow - origin[0];
					final int endAtRow = endRow - origin[0];
					final int startFromCol = startCol - origin[1];
					final int endAtCol = endCol - origin[1];

					for (int row = startFromRow; row < endAtRow; ++row) {
						for (int col = startFromCol; col < endAtCol; ++col) {
							float redSum = 0;
							float greenSum = 0;
							float blueSum = 0;
							for (int npos = 0; npos < kernelMatrix.length; ++npos) {
								int relativeRow = row + (kernelWidth - npos / kernelWidth - 1);
								int relativeCol = col + (kernelWidth - npos % kernelWidth - 1);
								if (relativeRow < startRow)
									relativeRow += kernelHeight;
								else if (relativeRow >= endRow)
									relativeRow -= kernelHeight;
								if (relativeCol < startCol)
									relativeCol += kernelWidth;
								else if (relativeCol >= endCol)
									relativeCol -= kernelWidth;

								int pos = (relativeRow * 4 * imageWidth) + (relativeCol * 4) + 1;
								blueSum += kernelMatrix[npos] * ((int) pixels[pos++] & 0xFF);
								greenSum += kernelMatrix[npos] * ((int) pixels[pos++] & 0xFF);
								redSum += kernelMatrix[npos] * ((int) pixels[pos] & 0xFF);
							}

							int pos = ((row + origin[0]) * 4 * imageWidth) + ((col + origin[1]) * 4);
							newPixels[pos++] = (byte) 0xFF;
							newPixels[pos++] = (byte) blueSum;
							newPixels[pos++] = (byte) greenSum;
							newPixels[pos] = (byte) redSum;
						}
					}
					return null;
				}
			});
		}

		for (int threadNumber = 0; threadNumber < numberOfThreadsToProcessOn; ++threadNumber) {
			try {
				applyKernelMultithreadCompletition.take();
			} catch (InterruptedException e) {
				System.out.println("Exception caught while processing the image on multiple threads.");
			}
		}

		return createBufferedImageFromByteArray(newPixels, imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
	}
}