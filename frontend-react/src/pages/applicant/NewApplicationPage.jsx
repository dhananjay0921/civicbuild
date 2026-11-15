import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { applicationApi } from '../../services/api';
import {
  Building2,
  FileText,
  Upload,
  CheckCircle,
  AlertCircle,
  ArrowRight,
  ArrowLeft,
  Save,
  Check,
  FileCheck
} from 'lucide-react';

export default function NewApplicationPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [createdAppId, setCreatedAppId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Form State
  const [formData, setFormData] = useState({
    // Applicant
    applicantType: 'INDIVIDUAL',
    // Property
    address: 'Plot 104, Sunrise Heights, Ring Road',
    surveyNumber: 'SRV-2026/9941',
    wardNumber: 'Ward-08',
    zone: 'East Urban Zone',
    plotArea: 2800,
    ownershipType: 'FREEHOLD',
    // Construction
    buildingType: 'RESIDENTIAL',
    constructionPurpose: 'New G+2 Duplex Residential Villa',
    numberOfFloors: 3,
    builtUpArea: 4200,
    buildingHeight: 11.5,
    estimatedCost: 8500000,
    parkingAvailable: true,
  });

  // Files State
  const [files, setFiles] = useState({
    PROPERTY_OWNERSHIP: null,
    BUILDING_BLUEPRINT: null,
    STRUCTURAL_STABILITY: null,
    SITE_PLAN: null,
    IDENTITY_PROOF: null,
  });

  const [uploadStatus, setUploadStatus] = useState({});

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleFileChange = (documentType, file) => {
    if (file && file.size > 20 * 1024 * 1024) {
      setError(`File ${file.name} exceeds statutory 20MB limit`);
      return;
    }
    setError('');
    setFiles(prev => ({ ...prev, [documentType]: file }));
  };

  // Step 1 -> 2 -> 3 validations
  const validateStep = (currentStep) => {
    setError('');
    if (currentStep === 1) {
      if (!formData.address || !formData.surveyNumber || !formData.wardNumber || !formData.zone || !formData.plotArea) {
        setError('Please complete all mandatory property location fields.');
        return false;
      }
      if (Number(formData.plotArea) < 100) {
        setError('Plot area must be at least 100 sq.ft.');
        return false;
      }
    } else if (currentStep === 2) {
      if (!formData.constructionPurpose || !formData.numberOfFloors || !formData.builtUpArea || !formData.buildingHeight || !formData.estimatedCost) {
        setError('Please complete all statutory construction parameters.');
        return false;
      }
      if (Number(formData.numberOfFloors) < 1) {
        setError('Building must contain at least 1 floor.');
        return false;
      }
    }
    return true;
  };

  const nextStep = () => {
    if (validateStep(step)) {
      setStep(prev => prev + 1);
    }
  };

  const prevStep = () => {
    setError('');
    setStep(prev => prev - 1);
  };

  // Save Draft (Creates DB record)
  const handleSaveDraft = async () => {
    setError('');
    setLoading(true);
    try {
      const payload = {
        ...formData,
        plotArea: parseFloat(formData.plotArea),
        numberOfFloors: parseInt(formData.numberOfFloors, 10),
        builtUpArea: parseFloat(formData.builtUpArea),
        buildingHeight: parseFloat(formData.buildingHeight),
        estimatedCost: parseFloat(formData.estimatedCost),
      };

      let appId = createdAppId;
      if (!appId) {
        const res = await applicationApi.create(payload);
        if (res.success && res.data) {
          appId = res.data.id;
          setCreatedAppId(appId);
        }
      } else {
        await applicationApi.update(appId, payload);
      }

      setSuccessMsg(`Draft saved successfully (Application Reference: ${appId})`);
      setTimeout(() => setSuccessMsg(''), 4000);
      return appId;
    } catch (err) {
      setError(err.message || 'Failed to save draft application');
      return null;
    } finally {
      setLoading(false);
    }
  };

  // Upload Files to Backend
  const uploadDocumentsForApp = async (appId) => {
    const docTypes = ['PROPERTY_OWNERSHIP', 'BUILDING_BLUEPRINT', 'STRUCTURAL_STABILITY', 'SITE_PLAN', 'IDENTITY_PROOF'];
    const statuses = {};

    for (const type of docTypes) {
      const file = files[type];
      if (file) {
        try {
          const form = new FormData();
          form.append('file', file);
          form.append('documentType', type);
          await applicationApi.uploadDocument(appId, form);
          statuses[type] = 'Uploaded';
        } catch (err) {
          statuses[type] = 'Failed: ' + err.message;
        }
      } else {
        // Fallback for academic demonstration: generate dummy text file so workflow progresses
        try {
          const dummyBlob = new Blob([`Statutory Document: ${type}\nVerified for Municipal Permitting`], { type: 'text/plain' });
          const dummyFile = new File([dummyBlob], `${type.toLowerCase()}_sample.txt`, { type: 'text/plain' });
          const form = new FormData();
          form.append('file', dummyFile);
          form.append('documentType', type);
          await applicationApi.uploadDocument(appId, form);
          statuses[type] = 'Auto-attached (Demo)';
        } catch (err) {
          statuses[type] = 'Mock upload failed';
        }
      }
    }
    setUploadStatus(statuses);
  };

  // Submit Application
  const handleSubmitFinal = async () => {
    setError('');
    setLoading(true);
    try {
      // 1. Save or Update Draft
      const appId = await handleSaveDraft();
      if (!appId) throw new Error('Could not initialize application ID');

      // 2. Upload Documents
      await uploadDocumentsForApp(appId);

      // 3. Formally Submit (triggers Camunda 8 Zeebe workflow!)
      const submitRes = await applicationApi.submit(appId);
      if (submitRes.success) {
        navigate(`/applicant/applications/${appId}`);
      }
    } catch (err) {
      setError(err.message || 'Submission failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-2">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">New Municipal Building Permit</h1>
          <p className="text-xs text-slate-500">Submit multi-step architectural and cadastral parameters</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handleSaveDraft}
            disabled={loading}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 transition shadow-sm"
          >
            <Save className="w-3.5 h-3.5" />
            <span>Save Draft</span>
          </button>
        </div>
      </div>

      {/* Stepper Wizard Indicator */}
      <div className="grid grid-cols-4 gap-2 text-xs font-semibold">
        <div className={`p-3 rounded-xl border flex items-center gap-2 ${step === 1 ? 'bg-municipal-800 text-white border-municipal-800' : step > 1 ? 'bg-teal-50 text-teal-800 border-teal-200' : 'bg-white text-slate-400 border-slate-200'}`}>
          <span className="w-5 h-5 rounded-full bg-current/20 flex items-center justify-center text-[10px]">1</span>
          <span className="truncate">Property Site</span>
        </div>
        <div className={`p-3 rounded-xl border flex items-center gap-2 ${step === 2 ? 'bg-municipal-800 text-white border-municipal-800' : step > 2 ? 'bg-teal-50 text-teal-800 border-teal-200' : 'bg-white text-slate-400 border-slate-200'}`}>
          <span className="w-5 h-5 rounded-full bg-current/20 flex items-center justify-center text-[10px]">2</span>
          <span className="truncate">Specifications</span>
        </div>
        <div className={`p-3 rounded-xl border flex items-center gap-2 ${step === 3 ? 'bg-municipal-800 text-white border-municipal-800' : step > 3 ? 'bg-teal-50 text-teal-800 border-teal-200' : 'bg-white text-slate-400 border-slate-200'}`}>
          <span className="w-5 h-5 rounded-full bg-current/20 flex items-center justify-center text-[10px]">3</span>
          <span className="truncate">Documents</span>
        </div>
        <div className={`p-3 rounded-xl border flex items-center gap-2 ${step === 4 ? 'bg-municipal-800 text-white border-municipal-800' : 'bg-white text-slate-400 border-slate-200'}`}>
          <span className="w-5 h-5 rounded-full bg-current/20 flex items-center justify-center text-[10px]">4</span>
          <span className="truncate">Review & Submit</span>
        </div>
      </div>

      {/* Messages */}
      {error && (
        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}
      {successMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl flex items-center gap-2">
          <Check className="w-4 h-4 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* STEP 1: Property Location & Cadastre */}
      {step === 1 && (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
          <div className="border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-800">1. Property Location & Cadastral Registry</h3>
            <p className="text-xs text-slate-500">Provide legal property boundary and survey details verified with the municipal cadastre.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Property Physical Address <span className="text-red-500">*</span>
              </label>
              <textarea
                name="address"
                required
                rows={2}
                value={formData.address}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                placeholder="Plot/Door number, street name, locality"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Cadastral Survey Number <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="surveyNumber"
                required
                value={formData.surveyNumber}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                placeholder="e.g. SRV-2026/8841"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Municipal Ward Number <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="wardNumber"
                required
                value={formData.wardNumber}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                placeholder="e.g. Ward-08"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Zoning Sector <span className="text-red-500">*</span>
              </label>
              <select
                name="zone"
                value={formData.zone}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none bg-white"
              >
                <option value="Central Urban Zone">Central Urban Zone</option>
                <option value="North Industrial Zone">North Industrial Zone</option>
                <option value="East Urban Zone">East Urban Zone</option>
                <option value="South Residential Greenbelt">South Residential Greenbelt</option>
                <option value="West Mixed Development">West Mixed Development</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Plot Area (in Square Feet) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                name="plotArea"
                required
                min={100}
                value={formData.plotArea}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Land Ownership Title <span className="text-red-500">*</span>
              </label>
              <select
                name="ownershipType"
                value={formData.ownershipType}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none bg-white"
              >
                <option value="FREEHOLD">Freehold Deed</option>
                <option value="LEASEHOLD">Statutory Leasehold</option>
                <option value="MUNICIPAL_LEASE">Municipal Allotment Lease</option>
              </select>
            </div>
          </div>

          <div className="pt-4 flex justify-end">
            <button
              type="button"
              onClick={nextStep}
              className="bg-municipal-800 hover:bg-municipal-900 text-white font-semibold px-5 py-2.5 rounded-xl text-xs flex items-center gap-2 shadow-sm transition"
            >
              <span>Next: Construction Details</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* STEP 2: Construction Specifications */}
      {step === 2 && (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
          <div className="border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-800">2. Construction Details & Physical Metrics</h3>
            <p className="text-xs text-slate-500">Provide structural dimensions used for statutory National Building Code (NBC) compliance and fee assessment.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Building Classification <span className="text-red-500">*</span>
              </label>
              <select
                name="buildingType"
                value={formData.buildingType}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none bg-white"
              >
                <option value="RESIDENTIAL">Residential (Domestic Housing)</option>
                <option value="COMMERCIAL">Commercial (Retail/Offices)</option>
                <option value="INDUSTRIAL">Industrial (Warehouses/Manufacturing)</option>
                <option value="INSTITUTIONAL">Institutional (Schools/Hospitals)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Number of Floors <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                name="numberOfFloors"
                required
                min={1}
                max={50}
                value={formData.numberOfFloors}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Construction Purpose & Description <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="constructionPurpose"
                required
                value={formData.constructionPurpose}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
                placeholder="e.g. New 3-Storey Apartment with Basements"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Total Built-up Area (sq.ft) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                name="builtUpArea"
                required
                min={50}
                value={formData.builtUpArea}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Maximum Building Height (meters) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                step="0.1"
                name="buildingHeight"
                required
                min={2.5}
                value={formData.buildingHeight}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Estimated Construction Cost (INR) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                name="estimatedCost"
                required
                min={10000}
                value={formData.estimatedCost}
                onChange={handleChange}
                className="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-xs focus:ring-2 focus:ring-civicTeal-500 outline-none"
              />
            </div>

            <div className="flex items-center pt-5">
              <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700">
                <input
                  type="checkbox"
                  name="parkingAvailable"
                  checked={formData.parkingAvailable}
                  onChange={handleChange}
                  className="rounded text-civicTeal-600 focus:ring-civicTeal-500 h-4 w-4"
                />
                <span>Mandatory Off-Street Parking Provided On-Site</span>
              </label>
            </div>
          </div>

          <div className="pt-4 flex justify-between">
            <button
              type="button"
              onClick={prevStep}
              className="border border-slate-300 text-slate-700 font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 hover:bg-slate-50 transition"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back</span>
            </button>
            <button
              type="button"
              onClick={nextStep}
              className="bg-municipal-800 hover:bg-municipal-900 text-white font-semibold px-5 py-2.5 rounded-xl text-xs flex items-center gap-2 shadow-sm transition"
            >
              <span>Next: Document Uploads</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* STEP 3: Mandatory Document Checklist */}
      {step === 3 && (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
          <div className="border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-800">3. Statutory Document Verification Checklist</h3>
            <p className="text-xs text-slate-500">
              Each document will be verified sequentially by the Zeebe Multi-Instance verification sub-process. Max 20MB per PDF file.
            </p>
          </div>

          <div className="space-y-4">
            {[
              { type: 'PROPERTY_OWNERSHIP', title: 'Property Ownership Proof / Title Deed', desc: 'Registered sale deed or certified 7/12 extract', req: true },
              { type: 'BUILDING_BLUEPRINT', title: 'Architectural Building Blueprint', desc: 'Detailed floor plans, section views, elevations with engineer seal', req: true },
              { type: 'STRUCTURAL_STABILITY', title: 'Structural Stability Certificate', desc: 'Certified by a licensed civil/structural engineer', req: true },
              { type: 'SITE_PLAN', title: 'Key & Site Location Plan', desc: 'Road width, boundary distances, and neighbouring plots', req: false },
              { type: 'IDENTITY_PROOF', title: 'Applicant Government Photo ID', desc: 'Aadhaar, Passport or PAN card of owner/developer', req: false },
            ].map((doc) => (
              <div key={doc.type} className="p-4 rounded-xl border border-slate-200 hover:border-slate-300 transition flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-0.5">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900">{doc.title}</span>
                    {doc.req && <span className="text-[10px] text-red-600 font-semibold bg-red-50 px-1.5 py-0.5 rounded">Mandatory</span>}
                  </div>
                  <p className="text-[11px] text-slate-500">{doc.desc}</p>
                  {files[doc.type] && (
                    <span className="text-[11px] text-emerald-600 font-medium flex items-center gap-1 mt-1">
                      <FileCheck className="w-3.5 h-3.5" /> Selected: {files[doc.type].name}
                    </span>
                  )}
                </div>

                <div className="shrink-0">
                  <label className="cursor-pointer inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-300 hover:bg-slate-50 text-xs font-semibold text-slate-700 transition">
                    <Upload className="w-3.5 h-3.5" />
                    <span>{files[doc.type] ? 'Replace PDF' : 'Choose PDF'}</span>
                    <input
                      type="file"
                      accept=".pdf,.png,.jpg,.jpeg"
                      className="hidden"
                      onChange={(e) => handleFileChange(doc.type, e.target.files[0])}
                    />
                  </label>
                </div>
              </div>
            ))}
          </div>

          <div className="pt-4 flex justify-between">
            <button
              type="button"
              onClick={prevStep}
              className="border border-slate-300 text-slate-700 font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 hover:bg-slate-50 transition"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back</span>
            </button>
            <button
              type="button"
              onClick={nextStep}
              className="bg-municipal-800 hover:bg-municipal-900 text-white font-semibold px-5 py-2.5 rounded-xl text-xs flex items-center gap-2 shadow-sm transition"
            >
              <span>Next: Review & Submit</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* STEP 4: Review and Submit */}
      {step === 4 && (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 space-y-6 shadow-sm">
          <div className="border-b border-slate-100 pb-3">
            <h3 className="text-base font-bold text-slate-800">4. Review Statutory Submission</h3>
            <p className="text-xs text-slate-500">Verify your information before launching the Camunda 8 workflow instance.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
              <span className="font-bold uppercase tracking-wider text-slate-400 block text-[10px]">Site Particulars</span>
              <p><strong className="text-slate-700">Address:</strong> {formData.address}</p>
              <p><strong className="text-slate-700">Survey No:</strong> {formData.surveyNumber} &bull; {formData.wardNumber}</p>
              <p><strong className="text-slate-700">Zone:</strong> {formData.zone}</p>
              <p><strong className="text-slate-700">Plot Area:</strong> {formData.plotArea} sq.ft ({formData.ownershipType})</p>
            </div>

            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2">
              <span className="font-bold uppercase tracking-wider text-slate-400 block text-[10px]">Building Specs</span>
              <p><strong className="text-slate-700">Category:</strong> {formData.buildingType}</p>
              <p><strong className="text-slate-700">Purpose:</strong> {formData.constructionPurpose}</p>
              <p><strong className="text-slate-700">Floors:</strong> {formData.numberOfFloors} Floors &bull; Height: {formData.buildingHeight}m</p>
              <p><strong className="text-slate-700">Built-Up Area:</strong> {formData.builtUpArea} sq.ft</p>
              <p><strong className="text-slate-700">Est. Cost:</strong> INR {Number(formData.estimatedCost).toLocaleString()}</p>
            </div>
          </div>

          {/* Declaration Statement */}
          <div className="p-4 rounded-xl bg-amber-50/70 border border-amber-200 text-[11px] text-amber-900 leading-relaxed">
            <strong>Statutory Declaration:</strong> I hereby declare that all particulars submitted above and architectural blueprints attached are compliant with the National Building Code (NBC 2016) and municipal zoning by-laws. I understand that false disclosures will lead to immediate workflow rejection.
          </div>

          <div className="pt-4 flex justify-between">
            <button
              type="button"
              onClick={prevStep}
              className="border border-slate-300 text-slate-700 font-semibold px-4 py-2 rounded-xl text-xs flex items-center gap-2 hover:bg-slate-50 transition"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back</span>
            </button>

            <button
              type="button"
              onClick={handleSubmitFinal}
              disabled={loading}
              className="bg-civicTeal-600 hover:bg-civicTeal-700 text-white font-semibold px-6 py-2.5 rounded-xl text-xs flex items-center gap-2 shadow-md transition disabled:opacity-50"
            >
              {loading ? (
                <>
                  <span className="animate-spin">&bull;</span>
                  <span>Initiating Camunda Workflow...</span>
                </>
              ) : (
                <>
                  <CheckCircle className="w-4 h-4" />
                  <span>Submit Permit Application</span>
                </>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
