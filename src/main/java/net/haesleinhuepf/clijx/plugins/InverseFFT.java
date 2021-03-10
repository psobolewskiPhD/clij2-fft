package net.haesleinhuepf.clijx.plugins;

import org.jocl.NativePointerObject;
import org.scijava.plugin.Plugin;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasAuthor;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_inverseFFT")
public class InverseFFT  extends AbstractCLIJ2Plugin implements
CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, HasAuthor, HasClassifiedInputOutput, IsCategorized {
	
	@Override
	public boolean executeCL() {
		boolean result = runInverseFFT(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]));
		return result;
	}
	
	public static ClearCLBuffer runInverseFFT(CLIJ2 clij2, ClearCLBuffer gpuFFT, long[] dims) {

		// create GPU memory for FFT
		ClearCLBuffer out = clij2.create(dims, NativeTypeEnum.Float);

		runInverseFFT(clij2, gpuFFT, out);
		
		return out;
	}

	
	/**
	 * Run Inverse FFT on a CLBuffer
	 * 
	 * @param gpuFFT the input CLBuffer that represents an FFT in the format generated by ForwardFFT
	 *				should be float and an CLFFT friendly size
	 * @return - output image as CLBuffer
	 */
	public static boolean runInverseFFT(CLIJ2 clij2, ClearCLBuffer gpuFFT, ClearCLBuffer gpuImg) {
	
		// get the long pointers to in, out, context and queue.
		long l_in = ((NativePointerObject) (gpuFFT.getPeerPointer()
			.getPointer())).getNativePointer();
		long l_out = ((NativePointerObject) (gpuImg.getPeerPointer()
			.getPointer())).getNativePointer();
		long l_context = ((NativePointerObject) (clij2.getCLIJ().getClearCLContext()
			.getPeerPointer().getPointer())).getNativePointer();
		long l_queue = ((NativePointerObject) (clij2.getCLIJ().getClearCLContext()
			.getDefaultQueue().getPeerPointer().getPointer())).getNativePointer();

		if (gpuImg.getDimensions().length==2) {
			// call the native code that runs the inverse FFT
			clij2fftWrapper.fft2dinv_32f_lp((long) (gpuImg.getWidth()), gpuImg.getHeight(),
					l_in, l_out, l_context, l_queue);
		}
		
		if (gpuImg.getDimensions().length==3) {
			// call the native code that runs the inverse FFT
			clij2fftWrapper.fft3dinv_32f_lp((long) (gpuImg.getWidth()), gpuImg.getHeight(), gpuImg.getDepth(),
					l_in, l_out, l_context, l_queue);
		}
	
		return true;
	}
	
	@Override
	public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
		ClearCLBuffer in = (ClearCLBuffer) args[0];
		
		long[] dimensions=new long[in.getDimensions().length];
		
		// TODO this assumes even input dimension and no extension
		// the assumption is not good most of the time, so we'll have
		// to revisit this.  To do proper inverse need to keep track of 
		// original size
		dimensions[0]=in.getDimensions()[0]-2;
		
		for (int d=1;d<in.getDimensions().length;d++) {
			dimensions[d]=in.getDimensions()[d];
		}
		
		return getCLIJ2().create(dimensions, NativeTypeEnum.Float);
	}

	@Override
	public String getParameterHelpText() {
		return "Image input, ByRef Image destination";
	}

	@Override
	public String getDescription() {
		return "Performs inverse FFT";
	}

	@Override
	public String getAvailableForDimensions() {
		return "2D, 3D";
	}

	@Override
	public String getAuthorName() {
		return "Brian Northan";
	}

	@Override
	public String getInputType() {
		return "Image";
	}

	@Override
	public String getOutputType() {
		return "Image";
	}

	@Override
	public String getCategories() {
		return "Filter";
	}

}
