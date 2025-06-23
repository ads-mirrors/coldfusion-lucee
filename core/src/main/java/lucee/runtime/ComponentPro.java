package lucee.runtime;

import lucee.runtime.component.AbstractFinal;

interface ComponentPro extends Component {
	ComponentProperties getCurrentProperties();

	PageSource getCurrentPageSource();

	AbstractFinal getCurrentAbstractFinal();

	ComponentPro getCurrentBase();

}
