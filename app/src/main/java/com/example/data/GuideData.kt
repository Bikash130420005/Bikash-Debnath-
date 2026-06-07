package com.example.data

data class GuideSection(
    val id: String,
    val label: String,
    val icon: String,
    val title: String,
    val note: String,
    val steps: List<GuideStep> = emptyList(),
    val scopes: List<ScopeItem> = emptyList(),
    val envs: List<EnvItem> = emptyList(),
    val beats: List<BeatItem> = emptyList(),
    val measures: List<MeasureItem> = emptyList(),
    val methods: List<MethodItem> = emptyList(),
    val engines: List<EngineItem> = emptyList(),
    val errors: List<ErrorItem> = emptyList(),
    val incorrectImports: String? = null
)

data class GuideStep(
    val label: String,
    val code: String
)

data class ScopeItem(
    val scope: String,
    val desc: String
)

data class EnvItem(
    val env: String,
    val desc: String
)

data class BeatItem(
    val step: String,
    val call: String,
    val result: String
)

data class MeasureItem(
    val measure: String,
    val desc: String,
    val cls: String
)

data class MethodItem(
    val method: String,
    val result: String
)

data class EngineItem(
    val name: String,
    val desc: String
)

data class ErrorItem(
    val symptom: String,
    val cause: String,
    val fix: String
)

object GuideData {
    val SECTIONS = listOf(
        GuideSection(
            id = "install",
            label = "01 INSTALL",
            icon = "⬇️",
            title = "Installation",
            note = "Requires Python 3.10+. Always use a virtual environment — gs-quant pins numpy, pandas and scientific library versions.",
            steps = listOf(
                GuideStep(
                    label = "Create virtual environment",
                    code = """# macOS / Linux
python3 -m venv gsq-env
source gsq-env/bin/activate

# Windows (PowerShell)
python -m venv gsq-env
gsq-env\\Scripts\\Activate.ps1"""
                ),
                GuideStep(
                    label = "Install from PyPI",
                    code = """# Core library
pip install gs-quant

# With Jupyter & plotting extras
pip install gs-quant[notebook]"""
                ),
                GuideStep(
                    label = "Install from source (optional)",
                    code = """git clone https://github.com/goldmansachs/gs-quant.git
cd gs-quant
pip install -e .[notebook]"""
                ),
                GuideStep(
                    label = "Verify install",
                    code = """python -c "import gs_quant; print(gs_quant.__version__)" """
                )
            )
        ),
        GuideSection(
            id = "credentials",
            label = "02 CREDENTIALS",
            icon = "🔑",
            title = "Marquee Credentials",
            note = "Live pricing and market data require GS Marquee API credentials — provisioned to institutional clients by GS sales. The library itself is free (Apache-2.0). Never hardcode secrets in notebooks or commit them to GitHub.",
            steps = listOf(
                GuideStep(
                    label = "Store as environment variables (macOS/Linux)",
                    code = """# Add to ~/.bashrc or ~/.zshrc
export GSQ_CLIENT_ID="your_client_id"
export GSQ_CLIENT_SECRET="your_client_secret""""
                ),
                GuideStep(
                    label = "Store as environment variables (Windows PowerShell)",
                    code = """setx GSQ_CLIENT_ID "your_client_id"
setx GSQ_CLIENT_SECRET "your_client_secret""""
                )
            ),
            scopes = listOf(
                ScopeItem("run_analytics", "Price instruments, compute risk, resolve trades"),
                ScopeItem("read_product_data", "Read market & reference datasets"),
                ScopeItem("read_financial_data", "Access financial data endpoints"),
                ScopeItem("modify_product_data", "Upload / modify dataset content")
            )
        ),
        GuideSection(
            id = "auth",
            label = "03 AUTH",
            icon = "🔐",
            title = "Authenticate Session",
            note = "Initialise GsSession once at the top of your script or notebook — before constructing or pricing anything.",
            steps = listOf(
                GuideStep(
                    label = "OAuth2 — application credentials",
                    code = """import os
from gs_quant.session import GsSession, Environment

GsSession.use(
    environment_or_domain=Environment.PROD,
    client_id=os.environ['GSQ_CLIENT_ID'],
    client_secret=os.environ['GSQ_CLIENT_SECRET'],
    scopes=('run_analytics',)
)

# Verify session is active
print(GsSession.current)"""
                ),
                GuideStep(
                    label = "Context manager (scoped session)",
                    code = """with GsSession.get(Environment.PROD,
                    client_id='...',
                    client_secret='...'):
    price = swap.dollar_price()
    # Session closes when block exits"""
                ),
                GuideStep(
                    label = "Internal GS users (Kerberos / SSO)",
                    code = """# No credentials needed inside Goldman network
GsSession.use(Environment.PROD)"""
                )
            ),
            envs = listOf(
                EnvItem("Environment.PROD", "Production — live market data (default)"),
                EnvItem("Environment.QA", "QA / staging"),
                EnvItem("Environment.DEV", "Development")
            )
        ),
        GuideSection(
            id = "trade",
            label = "04 FIRST TRADE",
            icon = "📈",
            title = "Your First Priced Trade",
            note = "fixed_rate is None until you resolve. Resolve sends the trade to the analytics service which returns at-market terms.",
            steps = listOf(
                GuideStep(
                    label = "Full end-to-end: 10y USD Payer Swap",
                    code = """import os
from gs_quant.session import GsSession, Environment
from gs_quant.instrument import IRSwap
from gs_quant.risk import DollarPrice, IRDelta

# 1. Authenticate
GsSession.use(Environment.PROD,
    client_id=os.environ['GSQ_CLIENT_ID'],
    client_secret=os.environ['GSQ_CLIENT_SECRET'],
    scopes=('run_analytics',))

# 2. Construct — supply only what you know
swap = IRSwap('Pay', '10y', 'USD', name='10y USD Payer')

# 3. Resolve — service fills in par rate, premium, etc.
swap.resolve()
print('Par fixed rate:', swap.fixed_rate)  # e.g. 0.0345

# 4. Price
pv = swap.dollar_price()       # FloatWithInfo
print('PV (USD):', float(pv))

# 5. Risk
delta = swap.calc(IRDelta)     # DataFrameWithInfo — bucketed delta ladder
print(delta)"""
                )
            ),
            beats = listOf(
                BeatItem("Construct", "IRSwap('Pay','10y','USD')", "Unresolved; fixed_rate = None"),
                BeatItem("Resolve", "swap.resolve()", "Missing params filled from market"),
                BeatItem("Price", "swap.dollar_price()", "FloatWithInfo — present value"),
                BeatItem("Risk", "swap.calc(IRDelta)", "DataFrameWithInfo — delta ladder")
            )
        ),
        GuideSection(
            id = "portfolio",
            label = "05 PORTFOLIOS",
            icon = "📁",
            title = "Portfolios & Risk",
            note = "Batching instruments into a Portfolio is far faster than looping trade-by-trade — all resolve and calc calls go in a single request.",
            steps = listOf(
                GuideStep(
                    label = "Create and price a mixed portfolio",
                    code = """from gs_quant.instrument import IRSwap, IRSwaption
from gs_quant.markets.portfolio import Portfolio
from gs_quant.risk import DollarPrice, IRDelta, IRVega

swap = IRSwap('Pay', '10y', 'USD', name='USD 10y Payer')
swaption = IRSwaption('Receive', '10y', 'EUR',
                      expiration_date='1y', name='EUR 1y10y Rec')

book = Portfolio([swap, swaption], name='My Book')
book.resolve()

# One batched request — all measures at once
results = book.calc([DollarPrice, IRDelta, IRVega])

print(results[DollarPrice].aggregate())  # total PV
print(results[swap][IRDelta])            # one trade's delta"""
                ),
                GuideStep(
                    label = "Nested portfolios",
                    code = """usd_book = Portfolio([
    IRSwap('Pay', '5y', 'USD'),
    IRSwap('Receive', '10y', 'USD')
], name='USD Book')

eur_book = Portfolio([IRSwap('Pay', '5y', 'EUR')], name='EUR Book')
master   = Portfolio([usd_book, eur_book], name='Master Book')"""
                ),
                GuideStep(
                    label = "Portfolio operations",
                    code = """portfolio.append(IRSwap('Pay', '2y', 'GBP'))  # add
first    = portfolio[0]                         # by index
usd_swap = portfolio['USD 10y Payer']           # by name
len(portfolio)                                  # top-level count
portfolio.all_instruments                       # across all nesting"""
                )
            ),
            measures = listOf(
                MeasureItem("DollarPrice / Price", "PV in USD / local currency", "All"),
                MeasureItem("IRDelta / IRDeltaParallel", "Rate sensitivity (bucketed / parallel)", "Rates"),
                MeasureItem("IRVega", "Vol sensitivity", "Rates"),
                MeasureItem("IRGamma", "Convexity", "Rates"),
                MeasureItem("FXDelta / FXVega", "FX spot / vol sensitivity", "FX"),
                MeasureItem("EqDelta / EqVega", "Equity spot / vol sensitivity", "Equity")
            )
        ),
        GuideSection(
            id = "datasets",
            label = "06 DATASETS",
            icon = "📊",
            title = "Market Data via Datasets",
            note = "Each dataset defines its own filter keys (bbid, ticker, assetId…). Check the Marquee catalog for the correct kwarg. For equities, TREOD keyed by bbid is almost always right.",
            steps = listOf(
                GuideStep(
                    label = "Pull equity time series (TREOD)",
                    code = """import datetime as dt
from gs_quant.data import Dataset

ds = Dataset('TREOD')

# Date-range query — multiple symbols
df = ds.get_data(
    dt.date(2025, 1, 2), dt.date(2025, 3, 19),
    bbid=['GS UN', 'AAPL UW']
)
print(df.head())

# Single field as a pandas Series
s = ds.get_data_series('closePrice', bbid='AAPL UW')

# Most recent value only
last = ds.get_data_last(
    as_of=dt.datetime.now(), bbid='AAPL UW'
)

# What symbols does this dataset cover?
coverage = ds.get_coverage()"""
                )
            ),
            methods = listOf(
                MethodItem("get_data(start, end, **dims)", "DataFrame, one row per observation"),
                MethodItem("get_data_series(field, **dims)", "Single pandas Series"),
                MethodItem("get_data_last(as_of, **dims)", "Most recent value(s)"),
                MethodItem("get_coverage()", "Which symbols a dataset covers")
            )
        ),
        GuideSection(
            id = "backtest",
            label = "07 BACKTEST",
            icon = "⏪",
            title = "Backtesting",
            note = "Three concepts carry the backtester: Strategy (bundles triggers), Trigger (decides when to act), Action (decides what to do). Engine runs it across dates and builds the P&L series.",
            steps = listOf(
                GuideStep(
                    label = "Add a fresh 10y swap every month",
                    code = """import datetime as dt
from gs_quant.instrument import IRSwap
from gs_quant.backtests.strategy import Strategy
from gs_quant.backtests.triggers import (
    PeriodicTrigger, PeriodicTriggerRequirements)
from gs_quant.backtests.actions import AddTradeAction
from gs_quant.backtests.generic_engine import GenericEngine

swap   = IRSwap('Pay', '10y', 'USD')
action = AddTradeAction(swap)

trigger = PeriodicTrigger(
    PeriodicTriggerRequirements(
        start_date=dt.date(2025, 1, 1),
        end_date=dt.date(2025, 6, 1),
        frequency='1m'),
    actions=action)

strategy = Strategy(None, trigger)

result = GenericEngine().run_backtest(
    strategy,
    start=dt.date(2025, 1, 1),
    end=dt.date(2025, 6, 1))

print(result.result_summary())"""
                )
            ),
            engines = listOf(
                EngineItem("GenericEngine", "Multi-asset OTC strategies (default choice)"),
                EngineItem("EquityVolEngine", "Server-side equity-vol strategies (faster)"),
                EngineItem("PredefinedAssetEngine", "Custom order-based backtests")
            )
        ),
        GuideSection(
            id = "troubleshoot",
            label = "08 ERRORS",
            icon = "⚠️",
            title = "Troubleshooting",
            note = "Nine times out of ten: you forgot to authenticate, or you forgot to resolve.",
            errors = listOf(
                ErrorItem("Auth / 401 error", "No active session", "Call GsSession.use(...) before any pricing call"),
                ErrorItem("fixed_rate is None", "Trade not resolved", "Call swap.resolve() first"),
                ErrorItem("Empty / None result", "Wrong scope", "Request run_analytics scope on the session"),
                ErrorItem("Future has no value", "Inside PricingContext", "Call .result() after exiting the block"),
                ErrorItem("Dataset returns nothing", "Wrong dimension key", "Check the catalog for the right kwarg"),
                ErrorItem("ImportError on install", "Version clash", "Reinstall in a clean virtual environment"),
                ErrorItem("FX pair not recognised", "Wrong format", "Use ISO pair form e.g. 'EURUSD'")
            ),
            incorrectImports = """# Correct
from gs_quant.common import Currency, PayReceive

# WRONG — do not import from target.common
# from gs_quant.target.common import Currency"""
        )
    )
    
    // Quiz Questions for gs-quant learning
    val QUIZ_QUESTIONS = listOf(
        QuizQuestion(
            question = "Which method of GsSession is used for OAuth2 application credentials?",
            options = listOf("GsSession.start()", "GsSession.use()", "GsSession.connect()", "GsSession.init()"),
            correctIndex = 1,
            explanation = "GsSession.use(...) is the standard way to configure and authenticate the session globally."
        ),
        QuizQuestion(
            question = "Why is `fixed_rate` sometimes returned as `None` on a constructed Swap?",
            options = listOf(
                "The SDK has a bug",
                "The environment is set to DEV",
                "The trade hasn't been resolved using swap.resolve()",
                "The market is closed"
            ),
            correctIndex = 2,
            explanation = "You must call swap.resolve() first, which sends the trade to Goldman's analytics service to assign at-market terms."
        ),
        QuizQuestion(
            question = "Which scope is required to price instruments, compute risk, and resolve trades?",
            options = listOf("read_product_data", "run_analytics", "read_financial_data", "modify_product_data"),
            correctIndex = 1,
            explanation = "The 'run_analytics' scope authorizes pricing and calculator executions."
        ),
        QuizQuestion(
            question = "Which risk measure is used to calculate rate sensitivity?",
            options = listOf("IRVega", "IRDelta", "IRGamma", "EqDelta"),
            correctIndex = 1,
            explanation = "IRDelta (Interest Rate Delta) measures interest rate sensitivity, while IRVega measures interest rate volatility sensitivity."
        ),
        QuizQuestion(
            question = "Which Engine is typically the default choice for backtesting Multi-asset OTC strategies?",
            options = listOf("GenericEngine", "EquityVolEngine", "PredefinedAssetEngine", "FastEngine"),
            correctIndex = 0,
            explanation = "GenericEngine is the default, multi-asset engine for off-the-counter (OTC) derivative strategies."
        ),
        QuizQuestion(
            question = "Which dataset handles Goldman Sachs Equities end-of-day close prices?",
            options = listOf("GS_EQ_EOD", "TREOD", "EQUITY_LIVE", "EOD_CLOSE"),
            correctIndex = 1,
            explanation = "The TREOD dataset is the standard Goldman Sachs dataset to pull equities close prices in gs-quant."
        ),
        QuizQuestion(
            question = "Where should you never put your Marquee Client ID and Secret?",
            options = listOf("System environment variables", "Hardcoded in notebook scripts or public GitHub repositories", "In an encrypted config file", "In a local .env file listed in .gitignore"),
            correctIndex = 1,
            explanation = "Hardcoding credentials or committing them to source control exposes crucial Marquee API authorization."
        )
    )
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
